package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.enums.TaskStage;
import com.animegen.common.enums.TaskStatus;
import com.animegen.common.enums.WorkStatus;
import com.animegen.dao.domain.TaskDO;
import com.animegen.dao.domain.WorkDO;
import com.animegen.dao.mapper.TaskMapper;
import com.animegen.dao.mapper.WorkMapper;
import com.animegen.service.dto.CreateWorkRequest;
import com.animegen.service.dto.CreateWorkResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class WorkService {
    public static final String TASK_QUEUE_KEY = "queue:tasks";
    private static final Logger log = LoggerFactory.getLogger(WorkService.class);

    private final WorkMapper workMapper;
    private final TaskMapper taskMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public WorkService(WorkMapper workMapper, TaskMapper taskMapper, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.workMapper = workMapper;
        this.taskMapper = taskMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateWorkResponse createWork(Long userId, CreateWorkRequest request) {
        log.info("createWork start userId={}, traceId={}", userId, MDC.get("traceId"));
        String dedupeKey = buildDedupeKey(userId, request);
        String resultKey = dedupeKey + ":result";
        String lockKey = dedupeKey + ":lock";
        CreateWorkResponse cached = readCachedResponse(resultKey);
        if (cached != null) {
            log.info("createWork idempotent hit userId={}, requestId={}", userId, request.getRequestId());
            return cached;
        }
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            cached = readCachedResponse(resultKey);
            if (cached != null) {
                return cached;
            }
            throw new BizException(ErrorCodes.DUPLICATE_REQUEST_IN_PROGRESS, "duplicate request is still processing");
        }
        try {
            WorkDO work = new WorkDO();
            work.setUserId(userId);
            work.setTitle(request.getTitle());
            work.setPrompt(request.getPrompt());
            work.setStyleId(request.getStyleId());
            work.setAspectRatio(request.getAspectRatio());
            work.setDurationSec(request.getDurationSec());
            work.setStatus(WorkStatus.GENERATING.name());
            workMapper.insert(work);

            TaskDO task = new TaskDO();
            task.setWorkId(work.getId());
            task.setType("GENERATE_VIDEO");
            task.setStatus(TaskStatus.PENDING.name());
            task.setProgress(0);
            task.setStage(TaskStage.QUEUED.name());
            task.setRetryCount(0);
            String traceId = MDC.get("traceId");
            task.setTraceId(traceId == null || traceId.isBlank() ? UUID.randomUUID().toString().replace("-", "") : traceId);
            taskMapper.insert(task);

            enqueueTask(userId, work, task);
            writeTaskStatusCache(task);
            CreateWorkResponse response = new CreateWorkResponse(work.getId(), task.getId());
            try {
                redisTemplate.opsForValue().set(resultKey, objectMapper.writeValueAsString(response), 1, TimeUnit.DAYS);
            } catch (Exception ex) {
                log.warn("failed to cache idempotent result key={}", resultKey, ex);
            }
            log.info("createWork success userId={}, workId={}, taskId={}, traceId={}", userId, response.getWorkId(), response.getTaskId(), MDC.get("traceId"));
            return response;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(ErrorCodes.INTERNAL_ERROR, ex.getMessage());
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public List<WorkDO> listWorks(Long userId, Long cursor, Integer limit) {
        return workMapper.listByUser(userId, cursor == null ? 0L : cursor, limit == null ? 20 : limit);
    }

    public WorkDO getWork(Long userId, Long workId) {
        WorkDO workDO = workMapper.findById(workId);
        if (workDO == null || !userId.equals(workDO.getUserId())) {
            throw new BizException(ErrorCodes.WORK_NOT_FOUND, "work not found");
        }
        return workDO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWork(Long userId, Long workId) {
        int rows = workMapper.deleteById(workId, userId);
        if (rows == 0) {
            throw new BizException(ErrorCodes.WORK_NOT_FOUND, "work not found");
        }
        log.info("deleteWork success userId={}, workId={}, traceId={}", userId, workId, MDC.get("traceId"));
    }

    private void enqueueTask(Long userId, WorkDO work, TaskDO task) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskId", task.getId());
            payload.put("workId", work.getId());
            payload.put("userId", userId);
            payload.put("prompt", work.getPrompt());
            payload.put("styleId", work.getStyleId());
            payload.put("aspectRatio", work.getAspectRatio());
            payload.put("durationSec", work.getDurationSec());
            payload.put("traceId", task.getTraceId());
            redisTemplate.opsForList().leftPush(TASK_QUEUE_KEY, objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            throw new BizException(ErrorCodes.ENQUEUE_FAILED, "failed to enqueue task");
        }
    }

    private void writeTaskStatusCache(TaskDO task) {
        Map<String, String> status = new HashMap<>();
        status.put("status", task.getStatus());
        status.put("progress", String.valueOf(task.getProgress()));
        status.put("stage", task.getStage());
        String key = "task:status:" + task.getId();
        redisTemplate.opsForHash().putAll(key, status);
        redisTemplate.expire(key, Duration.ofDays(7));
    }

    private String buildDedupeKey(Long userId, CreateWorkRequest request) {
        String token = request.getRequestId();
        if (token == null || token.isBlank()) {
            String raw = userId + "|" + request.getTitle() + "|" + request.getPrompt() + "|" + request.getStyleId()
                    + "|" + request.getAspectRatio() + "|" + request.getDurationSec() + "|" + request.getMode();
            token = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        }
        return "idem:works:" + userId + ":" + token;
    }

    private CreateWorkResponse readCachedResponse(String key) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached == null || cached.isBlank()) {
                return null;
            }
            return objectMapper.readValue(cached, CreateWorkResponse.class);
        } catch (Exception ignore) {
            return null;
        }
    }
}
