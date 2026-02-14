package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.enums.TaskStatus;
import com.animegen.dao.domain.TaskDO;
import com.animegen.dao.domain.WorkDO;
import com.animegen.dao.mapper.TaskMapper;
import com.animegen.dao.mapper.WorkMapper;
import com.animegen.service.dto.TaskStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskMapper taskMapper;
    private final WorkMapper workMapper;

    public TaskService(TaskMapper taskMapper, WorkMapper workMapper) {
        this.taskMapper = taskMapper;
        this.workMapper = workMapper;
    }

    public TaskStatusResponse getTask(Long userId, Long taskId) {
        TaskDO taskDO = taskMapper.findByIdAndUser(taskId, userId);
        if (taskDO == null) {
            throw new BizException(ErrorCodes.TASK_NOT_FOUND, "task not found");
        }
        if (TaskStatus.SUCCESS.name().equals(taskDO.getStatus())) {
            WorkDO workDO = workMapper.findById(taskDO.getWorkId());
            if (workDO == null || workDO.getVideoUrl() == null || workDO.getVideoUrl().isBlank()) {
                throw new BizException(ErrorCodes.TASK_SUCCESS_WITHOUT_VIDEO, "invalid task state: success but empty video");
            }
        }
        return TaskStatusResponse.from(taskDO);
    }
}
