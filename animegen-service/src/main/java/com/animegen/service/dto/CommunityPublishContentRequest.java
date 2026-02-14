package com.animegen.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CommunityPublishContentRequest {
    @NotNull
    private Long workId;

    @NotBlank
    @Size(max = 128)
    private String title;

    @Size(max = 512)
    private String description;

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
