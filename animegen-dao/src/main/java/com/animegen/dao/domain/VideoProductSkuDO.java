package com.animegen.dao.domain;

import java.time.LocalDateTime;

public class VideoProductSkuDO {
    private Long id;
    private Long spuId;
    private Long videoWorkId;
    private Integer priceCent;
    private Integer originPriceCent;
    private String currency;
    private Integer validDays;
    private Integer stockTotal;
    private Integer stockAvailable;
    private Integer stockReserved;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public Long getVideoWorkId() { return videoWorkId; }
    public void setVideoWorkId(Long videoWorkId) { this.videoWorkId = videoWorkId; }
    public Integer getPriceCent() { return priceCent; }
    public void setPriceCent(Integer priceCent) { this.priceCent = priceCent; }
    public Integer getOriginPriceCent() { return originPriceCent; }
    public void setOriginPriceCent(Integer originPriceCent) { this.originPriceCent = originPriceCent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public Integer getStockTotal() { return stockTotal; }
    public void setStockTotal(Integer stockTotal) { this.stockTotal = stockTotal; }
    public Integer getStockAvailable() { return stockAvailable; }
    public void setStockAvailable(Integer stockAvailable) { this.stockAvailable = stockAvailable; }
    public Integer getStockReserved() { return stockReserved; }
    public void setStockReserved(Integer stockReserved) { this.stockReserved = stockReserved; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
