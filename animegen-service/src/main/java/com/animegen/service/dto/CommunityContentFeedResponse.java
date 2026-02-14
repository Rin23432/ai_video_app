package com.animegen.service.dto;

import java.util.List;

public class CommunityContentFeedResponse {
    private List<CommunityContentSummaryDTO> items;
    private Long nextCursor;

    public CommunityContentFeedResponse() {
    }

    public CommunityContentFeedResponse(List<CommunityContentSummaryDTO> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<CommunityContentSummaryDTO> getItems() { return items; }
    public void setItems(List<CommunityContentSummaryDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
