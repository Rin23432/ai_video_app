package com.animegen.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VideoMallPaymentCallbackRequest {
    @NotBlank
    private String channel;

    @NotBlank
    private String orderNo;

    @NotBlank
    private String channelTxnNo;

    @NotBlank
    private String status;

    @NotNull
    private Integer amountCent;

    private LocalDateTime paidAt;

    private String raw;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getChannelTxnNo() { return channelTxnNo; }
    public void setChannelTxnNo(String channelTxnNo) { this.channelTxnNo = channelTxnNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAmountCent() { return amountCent; }
    public void setAmountCent(Integer amountCent) { this.amountCent = amountCent; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }
}
