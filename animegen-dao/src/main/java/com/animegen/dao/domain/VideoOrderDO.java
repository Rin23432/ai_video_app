package com.animegen.dao.domain;

import java.time.LocalDateTime;

public class VideoOrderDO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer totalAmountCent;
    private Integer payAmountCent;
    private String currency;
    private String status;
    private String payChannel;
    private LocalDateTime payDeadlineAt;
    private LocalDateTime paidAt;
    private LocalDateTime closedAt;
    private Integer version;
    private String requestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getTotalAmountCent() { return totalAmountCent; }
    public void setTotalAmountCent(Integer totalAmountCent) { this.totalAmountCent = totalAmountCent; }
    public Integer getPayAmountCent() { return payAmountCent; }
    public void setPayAmountCent(Integer payAmountCent) { this.payAmountCent = payAmountCent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayChannel() { return payChannel; }
    public void setPayChannel(String payChannel) { this.payChannel = payChannel; }
    public LocalDateTime getPayDeadlineAt() { return payDeadlineAt; }
    public void setPayDeadlineAt(LocalDateTime payDeadlineAt) { this.payDeadlineAt = payDeadlineAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
