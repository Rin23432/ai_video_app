package com.animegen.service.dto;

import java.time.LocalDateTime;

public class CommunityContentSummaryDTO {
    private Long contentId;
    private String title;
    private String coverUrl;
    private CommunityAuthorDTO author;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private LocalDateTime publishTime;

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public CommunityAuthorDTO getAuthor() { return author; }
    public void setAuthor(CommunityAuthorDTO author) { this.author = author; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
