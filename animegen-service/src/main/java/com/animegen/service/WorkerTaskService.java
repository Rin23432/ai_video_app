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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerTaskService {
    private final TaskMapper taskMapper;
    private final WorkMapper workMapper;

    public WorkerTaskService(TaskMapper taskMapper, WorkMapper workMapper) {
        this.taskMapper = taskMapper;
        this.workMapper = workMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRunning(Long taskId, int retryCount) {
        TaskDO running = new TaskDO();
        running.setId(taskId);
        running.setStatus(TaskStatus.RUNNING.name());
        running.setProgress(5);
        running.setStage(TaskStage.VALIDATE.name());
        running.setRetryCount(retryCount);
        taskMapper.update(running);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(Long taskId, Long workId, int retryCount, String coverUrl, String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new BizException(ErrorCodes.TASK_SUCCESS_WITHOUT_VIDEO, "success without video url");
        }
        WorkDO work = new WorkDO();
        work.setId(workId);
        work.setStatus(WorkStatus.READY.name());
        work.setCoverUrl(coverUrl);
        work.setVideoUrl(videoUrl);
        workMapper.updateResult(work);

        TaskDO success = new TaskDO();
        success.setId(taskId);
        success.setStatus(TaskStatus.SUCCESS.name());
        success.setProgress(100);
        success.setStage(TaskStage.DONE.name());
        success.setRetryCount(retryCount);
        taskMapper.update(success);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRetry(Long taskId, int retryCount, String errorMessage) {
        TaskDO retry = new TaskDO();
        retry.setId(taskId);
        retry.setStatus(TaskStatus.PENDING.name());
        retry.setProgress(0);
        retry.setStage(TaskStage.QUEUED.name());
        retry.setRetryCount(retryCount);
        retry.setErrorCode("WORKER_RETRY");
        retry.setErrorMessage(errorMessage);
        taskMapper.update(retry);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markFail(Long taskId, Long workId, int retryCount, String errorMessage) {
        TaskDO fail = new TaskDO();
        fail.setId(taskId);
        fail.setStatus(TaskStatus.FAIL.name());
        fail.setProgress(100);
        fail.setStage(TaskStage.FAILED.name());
        fail.setRetryCount(retryCount);
        fail.setErrorCode("WORKER_ERROR");
        fail.setErrorMessage(errorMessage);
        taskMapper.update(fail);

        WorkDO work = new WorkDO();
        work.setId(workId);
        work.setStatus(WorkStatus.FAIL.name());
        workMapper.updateResult(work);
    }
}
