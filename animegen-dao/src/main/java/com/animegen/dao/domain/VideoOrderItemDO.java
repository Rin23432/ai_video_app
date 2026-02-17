package com.animegen.dao.domain;

import java.time.LocalDateTime;

public class VideoOrderItemDO {
    private Long id;
    private String orderNo;
    private Long skuId;
    private Long spuId;
    private Long workId;
    private String titleSnapshot;
    private String coverSnapshot;
    private Integer unitPriceCent;
    private Integer quantity;
    private Integer amountCent;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }
    public String getTitleSnapshot() { return titleSnapshot; }
    public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }
    public String getCoverSnapshot() { return coverSnapshot; }
    public void setCoverSnapshot(String coverSnapshot) { this.coverSnapshot = coverSnapshot; }
    public Integer getUnitPriceCent() { return unitPriceCent; }
    public void setUnitPriceCent(Integer unitPriceCent) { this.unitPriceCent = unitPriceCent; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getAmountCent() { return amountCent; }
    public void setAmountCent(Integer amountCent) { this.amountCent = amountCent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
