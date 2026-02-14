package com.animegen.service.dto;

import com.animegen.dao.domain.TaskDO;

public class TaskStatusResponse {
    private Long taskId;
    private String status;
    private Integer progress;
    private String stage;
    private String errorCode;
    private String errorMessage;

    public static TaskStatusResponse from(TaskDO taskDO) {
        TaskStatusResponse response = new TaskStatusResponse();
        response.setTaskId(taskDO.getId());
        response.setStatus(taskDO.getStatus());
        response.setProgress(taskDO.getProgress());
        response.setStage(taskDO.getStage());
        response.setErrorCode(taskDO.getErrorCode());
        response.setErrorMessage(taskDO.getErrorMessage());
        return response;
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
