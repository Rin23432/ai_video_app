package com.animegen.dao.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RankingSnapshotDO {
    private Long id;
    private String rankType;
    private String window;
    private LocalDate bizDate;
    private Integer rankNo;
    private Long entityId;
    private Double score;
    private String metaJson;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRankType() { return rankType; }
    public void setRankType(String rankType) { this.rankType = rankType; }
    public String getWindow() { return window; }
    public void setWindow(String window) { this.window = window; }
    public LocalDate getBizDate() { return bizDate; }
    public void setBizDate(LocalDate bizDate) { this.bizDate = bizDate; }
    public Integer getRankNo() { return rankNo; }
    public void setRankNo(Integer rankNo) { this.rankNo = rankNo; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getMetaJson() { return metaJson; }
    public void setMetaJson(String metaJson) { this.metaJson = metaJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
