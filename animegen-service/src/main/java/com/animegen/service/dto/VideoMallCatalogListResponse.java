package com.animegen.service.dto;

import java.util.List;

public class VideoMallCatalogListResponse {
    private List<VideoMallCatalogItemDTO> items;
    private Long nextCursor;

    public VideoMallCatalogListResponse() {
    }

    public VideoMallCatalogListResponse(List<VideoMallCatalogItemDTO> items, Long nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<VideoMallCatalogItemDTO> getItems() { return items; }
    public void setItems(List<VideoMallCatalogItemDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
