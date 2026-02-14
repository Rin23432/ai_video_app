package com.animegen.worker;

import com.animegen.ai.AiProvider;
import com.animegen.dao.domain.TaskDO;
import com.animegen.dao.mapper.TaskMapper;
import com.animegen.service.WorkerTaskService;
import com.animegen.common.enums.TaskStage;
import com.animegen.common.enums.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class TaskWorker {
    private static final String TASK_QUEUE_KEY = "queue:tasks";
    private static final int MAX_RETRY = 3;
    private static final int AI_TIMEOUT_SECONDS = 60;
    private static final Logger log = LoggerFactory.getLogger(TaskWorker.class);
    private final StringRedisTemplate redisTemplate;
    private final TaskMapper taskMapper;
    private final WorkerTaskService workerTaskService;
    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();

    public TaskWorker(StringRedisTemplate redisTemplate,
                      TaskMapper taskMapper,
                      WorkerTaskService workerTaskService,
                      AiProvider aiProvider,
                      ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.taskMapper = taskMapper;
        this.workerTaskService = workerTaskService;
        this.aiProvider = aiProvider;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        String payload = brpopPayload(Duration.ofSeconds(1));
        if (payload == null) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            Long taskId = Long.valueOf(String.valueOf(data.get("taskId")));
            Long workId = Long.valueOf(String.valueOf(data.get("workId")));
            String traceId = String.valueOf(data.getOrDefault("traceId", "worker-" + taskId));
            MDC.put("traceId", traceId);

            TaskDO current = taskMapper.findById(taskId);
            int retryCount = current != null && current.getRetryCount() != null ? current.getRetryCount() : 0;
            workerTaskService.markRunning(taskId, retryCount);
            writeTaskStatus(taskId, TaskStatus.RUNNING.name(), 5, TaskStage.VALIDATE.name(), null, null);

            AiProvider.VideoRequest request = new AiProvider.VideoRequest();
            request.setPrompt(String.valueOf(data.get("prompt")));
            request.setStyleId(String.valueOf(data.get("styleId")));
            request.setAspectRatio(String.valueOf(data.get("aspectRatio")));
            request.setDurationSec(Integer.valueOf(String.valueOf(data.get("durationSec"))));
            AiProvider.VideoResult result = generateWithTimeout(request);

            workerTaskService.markSuccess(taskId, workId, retryCount, result.getCoverUrl(), result.getVideoUrl());
            writeTaskStatus(taskId, TaskStatus.SUCCESS.name(), 100, TaskStage.DONE.name(), null, null);
            log.info("task success taskId={}, workId={}, retry={}", taskId, workId, retryCount);
        } catch (Exception ex) {
            handleFail(payload, ex.getMessage());
        } finally {
            MDC.remove("traceId");
        }
    }

    private String brpopPayload(Duration timeout) {
        List<byte[]> result = redisTemplate.execute((RedisConnection connection) ->
                connection.bRPop(Math.toIntExact(timeout.toSeconds()), TASK_QUEUE_KEY.getBytes(StandardCharsets.UTF_8)));
        if (result == null || result.size() < 2 || result.get(1) == null) {
            return null;
        }
        return new String(result.get(1), StandardCharsets.UTF_8);
    }

    private void handleFail(String payload, String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            Long taskId = Long.valueOf(String.valueOf(data.get("taskId")));
            Long workId = Long.valueOf(String.valueOf(data.get("workId")));
            String traceId = String.valueOf(data.getOrDefault("traceId", "worker-" + taskId));
            MDC.put("traceId", traceId);

            TaskDO current = taskMapper.findById(taskId);
            int currentRetry = current != null && current.getRetryCount() != null ? current.getRetryCount() : 0;
            if (currentRetry < MAX_RETRY) {
                int nextRetry = currentRetry + 1;
                workerTaskService.markRetry(taskId, nextRetry, message);
                redisTemplate.opsForList().leftPush(TASK_QUEUE_KEY, payload);
                writeTaskStatus(taskId, TaskStatus.PENDING.name(), 0, TaskStage.QUEUED.name(), "WORKER_RETRY", message);
                log.warn("task retry taskId={}, retry={}/{}, msg={}", taskId, nextRetry, MAX_RETRY, message);
            } else {
                workerTaskService.markFail(taskId, workId, currentRetry, message);
                writeTaskStatus(taskId, TaskStatus.FAIL.name(), 100, TaskStage.FAILED.name(), "WORKER_ERROR", message);
                log.error("task fail taskId={}, retries={}, msg={}", taskId, currentRetry, message);
            }
        } catch (Exception ignored) {
            log.error("handleFail failed payload={}", payload, ignored);
        } finally {
            MDC.remove("traceId");
        }
    }

    private void writeTaskStatus(Long taskId, String status, Integer progress, String stage, String errorCode, String errorMessage) {
        Map<String, String> map = new HashMap<>();
        map.put("status", status);
        map.put("progress", String.valueOf(progress));
        map.put("stage", stage);
        if (errorCode != null) {
            map.put("errorCode", errorCode);
        }
        if (errorMessage != null) {
            map.put("errorMessage", errorMessage);
        }
        String key = "task:status:" + taskId;
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, Duration.ofDays(7));
    }

    private AiProvider.VideoResult generateWithTimeout(AiProvider.VideoRequest request) throws Exception {
        Future<AiProvider.VideoResult> future = aiExecutor.submit(() -> aiProvider.generateVideo(request));
        try {
            return future.get(AI_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new RuntimeException("ai generate timeout");
        }
    }

    @PreDestroy
    public void shutdown() {
        aiExecutor.shutdownNow();
    }
}
