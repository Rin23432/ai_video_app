package com.animegen.service.dto;

import java.util.List;

public class VideoMallEntitlementListResponse {
    private List<VideoMallEntitlementItemDTO> items;
    private Long nextCursor;

    public VideoMallEntitlementListResponse() {
    }

    public VideoMallEntitlementListResponse(List<VideoMallEntitlementItemDTO> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<VideoMallEntitlementItemDTO> getItems() { return items; }
    public void setItems(List<VideoMallEntitlementItemDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
