package com.animegen.service.dto;

public class VideoMallCatalogItemDTO {
    private Long spuId;
    private Long skuId;
    private Long workId;
    private String title;
    private String coverUrl;
    private Integer priceCent;
    private Integer originPriceCent;
    private String currency;
    private Integer stockAvailable;
    private String status;

    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getPriceCent() { return priceCent; }
    public void setPriceCent(Integer priceCent) { this.priceCent = priceCent; }
    public Integer getOriginPriceCent() { return originPriceCent; }
    public void setOriginPriceCent(Integer originPriceCent) { this.originPriceCent = originPriceCent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
