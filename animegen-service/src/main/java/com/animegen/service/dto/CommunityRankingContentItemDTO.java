package com.animegen.service.dto;

public class CommunityRankingContentItemDTO {
    private Integer rank;
    private Long contentId;
    private String title;
    private String coverUrl;
    private CommunityAuthorDTO author;
    private Double score;
    private Double deltaScore;

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public CommunityAuthorDTO getAuthor() { return author; }
    public void setAuthor(CommunityAuthorDTO author) { this.author = author; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getDeltaScore() { return deltaScore; }
    public void setDeltaScore(Double deltaScore) { this.deltaScore = deltaScore; }
}
