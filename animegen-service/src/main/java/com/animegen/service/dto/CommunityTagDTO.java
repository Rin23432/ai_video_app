package com.animegen.service.dto;

public class CommunityTagDTO {
    private Long tagId;
    private String name;
    private Integer contentCount;
    private Long hotScore;

    public CommunityTagDTO() {
    }

    public CommunityTagDTO(Long tagId, String name, Integer contentCount, Long hotScore) {
        this.tagId = tagId;
        this.name = name;
        this.contentCount = contentCount;
        this.hotScore = hotScore;
    }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getContentCount() { return contentCount; }
    public void setContentCount(Integer contentCount) { this.contentCount = contentCount; }
    public Long getHotScore() { return hotScore; }
    public void setHotScore(Long hotScore) { this.hotScore = hotScore; }
}
