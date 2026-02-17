package com.animegen.service.dto;

import java.time.LocalDateTime;

public class VideoMallPaymentPrepayResponse {
    private String channel;
    private String prepayToken;
    private LocalDateTime expireAt;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getPrepayToken() { return prepayToken; }
    public void setPrepayToken(String prepayToken) { this.prepayToken = prepayToken; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
