package com.animegen.service.dto;

public class CommunityPublishContentResponse {
    private Long contentId;

    public CommunityPublishContentResponse() {
    }

    public CommunityPublishContentResponse(Long contentId) {
        this.contentId = contentId;
    }

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
}
