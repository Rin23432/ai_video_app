package com.animegen.service.dto;

public class CommunityRankingAuthorItemDTO {
    private Integer rank;
    private CommunityAuthorDTO author;
    private Integer publishedCount;
    private Integer likesReceived;
    private Double score;
    private Double deltaScore;

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public CommunityAuthorDTO getAuthor() { return author; }
    public void setAuthor(CommunityAuthorDTO author) { this.author = author; }
    public Integer getPublishedCount() { return publishedCount; }
    public void setPublishedCount(Integer publishedCount) { this.publishedCount = publishedCount; }
    public Integer getLikesReceived() { return likesReceived; }
    public void setLikesReceived(Integer likesReceived) { this.likesReceived = likesReceived; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getDeltaScore() { return deltaScore; }
    public void setDeltaScore(Double deltaScore) { this.deltaScore = deltaScore; }
}
