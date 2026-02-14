package com.animegen.service.dto;

import java.util.List;

public class CommunityCommentListResponse {
    private List<CommunityCommentDTO> items;
    private Long nextCursor;

    public CommunityCommentListResponse() {
    }

    public CommunityCommentListResponse(List<CommunityCommentDTO> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<CommunityCommentDTO> getItems() { return items; }
    public void setItems(List<CommunityCommentDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
