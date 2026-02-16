package com.animegen.service.dto;

import java.util.List;

public class CommunityRankingContentResponse {
    private String window;
    private List<CommunityRankingContentItemDTO> items;
    private Long nextCursor;

    public CommunityRankingContentResponse() {
    }

    public CommunityRankingContentResponse(String window, List<CommunityRankingContentItemDTO> items, Long nextCursor) {
        this.window = window;
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public String getWindow() { return window; }
    public void setWindow(String window) { this.window = window; }
    public List<CommunityRankingContentItemDTO> getItems() { return items; }
    public void setItems(List<CommunityRankingContentItemDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
