package com.animegen.service.dto;

public class CommunityViewerStateDTO {
    private boolean liked;
    private boolean favorited;

    public CommunityViewerStateDTO() {
    }

    public CommunityViewerStateDTO(boolean liked, boolean favorited) {
        this.liked = liked;
        this.favorited = favorited;
    }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public boolean isFavorited() { return favorited; }
    public void setFavorited(boolean favorited) { this.favorited = favorited; }
}
