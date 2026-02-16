package com.animegen.service.dto;

public class MeResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String role;
    private UserStatsDTO stats;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public UserStatsDTO getStats() { return stats; }
    public void setStats(UserStatsDTO stats) { this.stats = stats; }
}

