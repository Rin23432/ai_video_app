package com.animegen.service.dto;

import java.util.List;

public class CommunityRankingTagResponse {
    private String window;
    private List<CommunityRankingTagItemDTO> items;
    private Long nextCursor;

    public CommunityRankingTagResponse() {
    }

    public CommunityRankingTagResponse(String window, List<CommunityRankingTagItemDTO> items, Long nextCursor) {
        this.window = window;
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public String getWindow() { return window; }
    public void setWindow(String window) { this.window = window; }
    public List<CommunityRankingTagItemDTO> getItems() { return items; }
    public void setItems(List<CommunityRankingTagItemDTO> items) { this.items = items; }
    public Long getNextCursor() { return nextCursor; }
    public void setNextCursor(Long nextCursor) { this.nextCursor = nextCursor; }
}
