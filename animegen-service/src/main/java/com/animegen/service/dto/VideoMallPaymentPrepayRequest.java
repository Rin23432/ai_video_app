package com.animegen.service.dto;

import jakarta.validation.constraints.NotBlank;

public class VideoMallPaymentPrepayRequest {
    @NotBlank
    private String channel;

    private String returnUrl;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
