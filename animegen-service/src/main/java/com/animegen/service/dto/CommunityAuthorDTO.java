package com.animegen.service.dto;

public class CommunityAuthorDTO {
    private Long userId;
    private String nickname;
    private String avatarUrl;

    public CommunityAuthorDTO() {
    }

    public CommunityAuthorDTO(Long userId, String nickname, String avatarUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
