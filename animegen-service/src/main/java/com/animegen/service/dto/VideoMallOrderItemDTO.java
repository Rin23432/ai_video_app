package com.animegen.service.dto;

import java.time.LocalDateTime;

public class VideoMallOrderItemDTO {
    private String orderNo;
    private String status;
    private Integer payAmountCent;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPayAmountCent() { return payAmountCent; }
    public void setPayAmountCent(Integer payAmountCent) { this.payAmountCent = payAmountCent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
