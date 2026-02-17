package com.animegen.dao.domain;

import java.time.LocalDateTime;

public class VideoMallSkuViewDO {
    private Long skuId;
    private Long spuId;
    private Long workId;
    private String title;
    private String subtitle;
    private String description;
    private String coverUrl;
    private Integer priceCent;
    private Integer originPriceCent;
    private String currency;
    private Integer validDays;
    private Integer stockAvailable;
    private String status;
    private LocalDateTime createdAt;

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getPriceCent() { return priceCent; }
    public void setPriceCent(Integer priceCent) { this.priceCent = priceCent; }
    public Integer getOriginPriceCent() { return originPriceCent; }
    public void setOriginPriceCent(Integer originPriceCent) { this.originPriceCent = originPriceCent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
