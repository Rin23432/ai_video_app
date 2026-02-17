package com.animegen.service.dto;

import java.time.LocalDateTime;

public class VideoMallOrderCreateResponse {
    private String orderNo;
    private String status;
    private Integer payAmountCent;
    private String currency;
    private LocalDateTime payDeadlineAt;

    public VideoMallOrderCreateResponse() {
    }

    public VideoMallOrderCreateResponse(String orderNo, String status, Integer payAmountCent, String currency, LocalDateTime payDeadlineAt) {
        this.orderNo = orderNo;
        this.status = status;
        this.payAmountCent = payAmountCent;
        this.currency = currency;
        this.payDeadlineAt = payDeadlineAt;
    }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getPayAmountCent() { return payAmountCent; }
    public void setPayAmountCent(Integer payAmountCent) { this.payAmountCent = payAmountCent; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDateTime getPayDeadlineAt() { return payDeadlineAt; }
    public void setPayDeadlineAt(LocalDateTime payDeadlineAt) { this.payDeadlineAt = payDeadlineAt; }
}
