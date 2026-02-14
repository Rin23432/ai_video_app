package com.animegen.service.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GuestTokenRequest {
    @NotBlank
    @Size(max = 128)
    private String deviceId;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
