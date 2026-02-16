package com.animegen.service.auth;

public class AuthUserInfo {
    private Long userId;
    private String nickname;
    private String role;
    private String avatarUrl;

    public AuthUserInfo() {
    }

    public AuthUserInfo(Long userId, String nickname, String role, String avatarUrl) {
        this.userId = userId;
        this.nickname = nickname;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

