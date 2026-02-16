package com.animegen.service.ranking;

import java.util.List;

public class CommunityRankingEventPayload {
    private Long contentId;
    private Long authorId;
    private List<Long> tagIds;
    private Long scoreDelta;

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    public Long getScoreDelta() { return scoreDelta; }
    public void setScoreDelta(Long scoreDelta) { this.scoreDelta = scoreDelta; }
}
