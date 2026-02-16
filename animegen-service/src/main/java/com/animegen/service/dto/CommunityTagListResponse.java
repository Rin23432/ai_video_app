package com.animegen.service.dto;

import java.util.List;

public class CommunityTagListResponse {
    private List<CommunityTagDTO> items;

    public CommunityTagListResponse() {
    }

    public CommunityTagListResponse(List<CommunityTagDTO> items) {
        this.items = items;
    }

    public List<CommunityTagDTO> getItems() { return items; }
    public void setItems(List<CommunityTagDTO> items) { this.items = items; }
}
