package com.animegen.service.dto;

import java.time.LocalDateTime;

public class VideoMallPlayAuthResponse {
    private Boolean allowed;
    private String reason;
    private String playToken;
    private String playUrl;
    private LocalDateTime expireAt;

    public Boolean getAllowed() { return allowed; }
    public void setAllowed(Boolean allowed) { this.allowed = allowed; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPlayToken() { return playToken; }
    public void setPlayToken(String playToken) { this.playToken = playToken; }
    public String getPlayUrl() { return playUrl; }
    public void setPlayUrl(String playUrl) { this.playUrl = playUrl; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
