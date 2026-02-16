package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.dao.domain.UserDO;
import com.animegen.dao.mapper.ContentFavoriteMapper;
import com.animegen.dao.mapper.ContentMapper;
import com.animegen.dao.mapper.UserMapper;
import com.animegen.service.dto.MeResponse;
import com.animegen.service.dto.UpdateProfileRequest;
import com.animegen.service.dto.UserStatsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeService {
    private final UserMapper userMapper;
    private final ContentMapper contentMapper;
    private final ContentFavoriteMapper contentFavoriteMapper;

    public MeService(UserMapper userMapper,
                     ContentMapper contentMapper,
                     ContentFavoriteMapper contentFavoriteMapper) {
        this.userMapper = userMapper;
        this.contentMapper = contentMapper;
        this.contentFavoriteMapper = contentFavoriteMapper;
    }

    public MeResponse getMe(Long userId) {
        UserDO user = requireUser(userId);
        MeResponse response = new MeResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(defaultNickname(user));
        response.setAvatarUrl(user.getAvatarUrl());
        response.setBio(defaultBio(user.getBio()));
        response.setRole(user.getRole());
        response.setStats(new UserStatsDTO(
                contentMapper.countPublishedByAuthor(userId),
                contentFavoriteMapper.countByUserId(userId),
                contentMapper.sumLikesReceived(userId)
        ));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public MeResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserDO user = requireUser(userId);
        int rows = userMapper.updateProfile(
                userId,
                request.getNickname().trim(),
                trimToNull(request.getBio()),
                trimToNull(request.getAvatarUrl())
        );
        if (rows <= 0) {
            throw new BizException(ErrorCodes.INTERNAL_ERROR, "update profile failed");
        }
        user.setNickname(request.getNickname().trim());
        user.setBio(trimToNull(request.getBio()));
        user.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        return getMe(userId);
    }

    private UserDO requireUser(Long userId) {
        UserDO user = userMapper.findById(userId);
        if (user == null) {
            throw new BizException(ErrorCodes.LOGIN_REQUIRED, "login required");
        }
        return user;
    }

    private String defaultNickname(UserDO user) {
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername();
        }
        return "Guest";
    }

    private String defaultBio(String bio) {
        if (bio == null || bio.trim().isEmpty()) {
            return "This user is mysterious...";
        }
        return bio;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
