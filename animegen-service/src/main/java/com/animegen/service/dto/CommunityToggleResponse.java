package com.animegen.service.dto;

public class CommunityToggleResponse {
    private boolean liked;
    private boolean favorited;
    private Integer likeCount;
    private Integer favoriteCount;

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public boolean isFavorited() { return favorited; }
    public void setFavorited(boolean favorited) { this.favorited = favorited; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
}
