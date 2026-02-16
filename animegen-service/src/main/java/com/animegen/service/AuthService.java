package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.JwtUtils;
import com.animegen.dao.domain.UserDO;
import com.animegen.dao.mapper.UserMapper;
import com.animegen.service.auth.AuthTokenResponse;
import com.animegen.service.auth.AuthUserInfo;
import com.animegen.service.auth.GuestTokenResponse;
import com.animegen.service.auth.LoginRequest;
import com.animegen.service.auth.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class AuthService {
    private final UserMapper userMapper;

    @Value("${animegen.jwt.secret}")
    private String jwtSecret;

    @Value("${animegen.jwt.ttl-seconds:604800}")
    private long jwtTtlSeconds;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public GuestTokenResponse issueGuestToken(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new BizException(ErrorCodes.DEVICE_ID_REQUIRED, "deviceId is required");
        }
        UserDO user = userMapper.findByDeviceId(deviceId);
        if (user == null) {
            user = newGuestUser(deviceId);
            try {
                userMapper.insert(user);
            } catch (DuplicateKeyException ex) {
                user = userMapper.findByDeviceId(deviceId);
            }
        }
        if (user == null || user.getId() == null) {
            throw new BizException(ErrorCodes.INTERNAL_ERROR, "failed to create guest user");
        }
        String token = issueToken(user.getId());
        AuthUserInfo userInfo = toUserInfo(user);
        return new GuestTokenResponse(token, userInfo);
    }

    public AuthTokenResponse login(LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        UserDO user = userMapper.findByUsername(username);
        if (user == null || user.getPasswordHash() == null || !verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCodes.LOGIN_REQUIRED, "username or password is invalid");
        }
        return new AuthTokenResponse(issueToken(user.getId()), toUserInfo(user));
    }

    @Transactional(rollbackFor = Exception.class)
    public AuthTokenResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        UserDO user = new UserDO();
        user.setUsername(username);
        user.setPasswordHash(hashPassword(request.getPassword()));
        user.setNickname(trimOrDefault(request.getNickname(), username));
        user.setRole("USER");
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException ex) {
            throw new BizException(ErrorCodes.INVALID_PARAM, "username already exists");
        }
        return new AuthTokenResponse(issueToken(user.getId()), toUserInfo(user));
    }

    private UserDO newGuestUser(String deviceId) {
        UserDO user = new UserDO();
        user.setDeviceId(deviceId);
        user.setUsername(null);
        user.setNickname(buildGuestNickname(deviceId));
        user.setRole("GUEST");
        return user;
    }

    private AuthUserInfo toUserInfo(UserDO user) {
        return new AuthUserInfo(user.getId(), safeNickname(user), safeRole(user.getRole()), user.getAvatarUrl());
    }

    private String buildGuestNickname(String deviceId) {
        String sanitized = deviceId.replaceAll("[^a-zA-Z0-9_\\-]", "");
        if (sanitized.length() > 8) {
            sanitized = sanitized.substring(sanitized.length() - 8);
        }
        if (sanitized.isBlank()) {
            sanitized = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        }
        return "Guest-" + sanitized;
    }

    private String safeNickname(UserDO user) {
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            return "Guest";
        }
        return user.getNickname();
    }

    private String safeRole(String role) {
        if (role == null || role.isBlank()) {
            return "GUEST";
        }
        return role;
    }

    private String issueToken(Long userId) {
        return JwtUtils.issueToken(String.valueOf(userId), jwtSecret, jwtTtlSeconds);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new BizException(ErrorCodes.INVALID_PARAM, "username is required");
        }
        String normalized = username.trim();
        if (normalized.isEmpty()) {
            throw new BizException(ErrorCodes.INVALID_PARAM, "username is required");
        }
        return normalized;
    }

    private String trimOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String hashPassword(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BizException(ErrorCodes.INTERNAL_ERROR, "password hash failed");
        }
    }

    private boolean verifyPassword(String raw, String storedHash) {
        return hashPassword(raw).equals(storedHash);
    }
}
