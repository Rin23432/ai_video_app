package com.animegen.service.dto;

public class UserStatsDTO {
    private int published;
    private int favorites;
    private int likesReceived;

    public UserStatsDTO() {
    }

    public UserStatsDTO(int published, int favorites, int likesReceived) {
        this.published = published;
        this.favorites = favorites;
        this.likesReceived = likesReceived;
    }

    public int getPublished() { return published; }
    public void setPublished(int published) { this.published = published; }
    public int getFavorites() { return favorites; }
    public void setFavorites(int favorites) { this.favorites = favorites; }
    public int getLikesReceived() { return likesReceived; }
    public void setLikesReceived(int likesReceived) { this.likesReceived = likesReceived; }
}

