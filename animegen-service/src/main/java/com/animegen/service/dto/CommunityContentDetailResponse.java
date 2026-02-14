package com.animegen.service.dto;

import java.time.LocalDateTime;

public class CommunityContentDetailResponse {
    private Long contentId;
    private Long workId;
    private String title;
    private String description;
    private String mediaType;
    private String coverUrl;
    private String mediaUrl;
    private CommunityAuthorDTO author;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private CommunityViewerStateDTO viewerState;
    private LocalDateTime publishTime;

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public CommunityAuthorDTO getAuthor() { return author; }
    public void setAuthor(CommunityAuthorDTO author) { this.author = author; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public CommunityViewerStateDTO getViewerState() { return viewerState; }
    public void setViewerState(CommunityViewerStateDTO viewerState) { this.viewerState = viewerState; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
