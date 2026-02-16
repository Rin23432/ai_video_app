package com.animegen.service.dto;

public class CommunityRankingTagItemDTO {
    private Integer rank;
    private Long tagId;
    private String name;
    private Integer contentCount;
    private Double score;
    private Double deltaScore;

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getContentCount() { return contentCount; }
    public void setContentCount(Integer contentCount) { this.contentCount = contentCount; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getDeltaScore() { return deltaScore; }
    public void setDeltaScore(Double deltaScore) { this.deltaScore = deltaScore; }
}
