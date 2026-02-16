package com.animegen.service.dto;

import java.util.List;

public class CommunityRankingAuthorResponse {
    private String window;
    private List<CommunityRankingAuthorItemDTO> items;
    private Long nextCursor;

    public CommunityRankingAuthorResponse() {
    }

    public CommunityRankingAuthorResponse(String window, List<CommunityRankingAuthorItemDTO> items, Long nextCursor) {
        this.window = window;
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public String getWindow() { return window; }
    public void setWindow(String window) { this.window = window; }
    public List<CommunityRankingAuthorItemDTO> getItems() { return items; }
    public void setItems(List<CommunityRankingAuthorItemDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
