package com.animegen.service.dto;

import java.time.LocalDateTime;

public class VideoMallEntitlementItemDTO {
    private Long workId;
    private String title;
    private String coverUrl;
    private LocalDateTime grantedAt;
    private LocalDateTime expireAt;
    private String status;

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
