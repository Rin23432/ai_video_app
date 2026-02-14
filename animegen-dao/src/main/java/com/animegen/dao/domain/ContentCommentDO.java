package com.animegen.dao.domain;

import java.time.LocalDateTime;

public class ContentCommentDO {
    private Long id;
    private Long contentId;
    private Long userId;
    private String text;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
