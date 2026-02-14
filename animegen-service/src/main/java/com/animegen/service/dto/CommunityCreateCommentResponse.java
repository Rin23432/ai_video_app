package com.animegen.service.dto;

public class CommunityCreateCommentResponse {
    private Long commentId;
    private Integer commentCount;

    public CommunityCreateCommentResponse() {
    }

    public CommunityCreateCommentResponse(Long commentId, Integer commentCount) {
        this.commentId = commentId;
        this.commentCount = commentCount;
    }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
}
