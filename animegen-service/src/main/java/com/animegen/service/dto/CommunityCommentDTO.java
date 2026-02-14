package com.animegen.service.dto;

import java.time.LocalDateTime;

public class CommunityCommentDTO {
    private Long commentId;
    private CommunityAuthorDTO user;
    private String text;
    private LocalDateTime createdAt;

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public CommunityAuthorDTO getUser() { return user; }
    public void setUser(CommunityAuthorDTO user) { this.user = user; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
