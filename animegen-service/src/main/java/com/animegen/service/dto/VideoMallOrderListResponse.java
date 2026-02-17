package com.animegen.service.dto;

import java.util.List;

public class VideoMallOrderListResponse {
    private List<VideoMallOrderItemDTO> items;
    private Long nextCursor;

    public VideoMallOrderListResponse() {
    }

    public VideoMallOrderListResponse(List<VideoMallOrderItemDTO> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<VideoMallOrderItemDTO> getItems() { return items; }
    public void setItems(List<VideoMallOrderItemDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
