package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.JwtUtils;
import com.animegen.service.auth.GuestTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Value("${animegen.jwt.secret}")
    private String jwtSecret;

    public GuestTokenResponse issueGuestToken(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new BizException(ErrorCodes.DEVICE_ID_REQUIRED, "deviceId is required");
        }
        long userId = Math.abs(deviceId.hashCode());
        String token = JwtUtils.issueToken(String.valueOf(userId), jwtSecret, 7 * 24 * 3600L);
        return new GuestTokenResponse(token, userId);
    }
}
