package com.animegen.service.dto;

public class CreateWorkResponse {
    private Long workId;
    private Long taskId;

    public CreateWorkResponse() {
    }

    public CreateWorkResponse(Long workId, Long taskId) {
        this.workId = workId;
        this.taskId = taskId;
    }

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
}
