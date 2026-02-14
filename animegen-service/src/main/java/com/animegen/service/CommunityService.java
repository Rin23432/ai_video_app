package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.enums.WorkStatus;
import com.animegen.dao.domain.*;
import com.animegen.dao.mapper.*;
import com.animegen.service.dto.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommunityService {
    private static final String HOT_TAB = "hot";
    private static final String HOT_ZSET_KEY = "zset:content:hot";

    private final WorkMapper workMapper;
    private final UserMapper userMapper;
    private final ContentMapper contentMapper;
    private final ContentLikeMapper contentLikeMapper;
    private final ContentFavoriteMapper contentFavoriteMapper;
    private final ContentCommentMapper contentCommentMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final StringRedisTemplate redisTemplate;

    public CommunityService(WorkMapper workMapper,
                            UserMapper userMapper,
                            ContentMapper contentMapper,
                            ContentLikeMapper contentLikeMapper,
                            ContentFavoriteMapper contentFavoriteMapper,
                            ContentCommentMapper contentCommentMapper,
                            SensitiveWordFilter sensitiveWordFilter,
                            StringRedisTemplate redisTemplate) {
        this.workMapper = workMapper;
        this.userMapper = userMapper;
        this.contentMapper = contentMapper;
        this.contentLikeMapper = contentLikeMapper;
        this.contentFavoriteMapper = contentFavoriteMapper;
        this.contentCommentMapper = contentCommentMapper;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityPublishContentResponse publish(Long userId, CommunityPublishContentRequest request) {
        WorkDO workDO = workMapper.findById(request.getWorkId());
        if (workDO == null || !userId.equals(workDO.getUserId())) {
            throw new BizException(ErrorCodes.WORK_NOT_FOUND, "work not found");
        }
        if (!WorkStatus.READY.name().equals(workDO.getStatus())
                || isBlank(workDO.getVideoUrl())
                || isBlank(workDO.getCoverUrl())) {
            throw new BizException(ErrorCodes.CONTENT_NOT_READY, "work is not ready for publish");
        }
        ContentDO existed = contentMapper.findByWorkId(workDO.getId());
        if (existed != null) {
            throw new BizException(ErrorCodes.CONTENT_ALREADY_PUBLISHED, "work already published");
        }
        ContentDO contentDO = new ContentDO();
        contentDO.setWorkId(workDO.getId());
        contentDO.setAuthorId(userId);
        contentDO.setTitle(request.getTitle().trim());
        contentDO.setDescription(trimToNull(request.getDescription()));
        contentDO.setMediaType("VIDEO");
        contentDO.setCoverUrl(workDO.getCoverUrl());
        contentDO.setMediaUrl(workDO.getVideoUrl());
        contentDO.setStatus("PUBLISHED");
        contentDO.setPublishTime(LocalDateTime.now());
        try {
            contentMapper.insert(contentDO);
        } catch (DuplicateKeyException ex) {
            throw new BizException(ErrorCodes.CONTENT_ALREADY_PUBLISHED, "work already published");
        }
        redisTemplate.opsForZSet().add(HOT_ZSET_KEY, String.valueOf(contentDO.getId()), 0.0D);
        return new CommunityPublishContentResponse(contentDO.getId());
    }

    public CommunityContentFeedResponse listContents(String tab, Long cursor, Integer limit) {
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows;
        if (HOT_TAB.equalsIgnoreCase(tab)) {
            rows = listHot(offset, size);
        } else {
            rows = contentMapper.listPublishedByLatest(offset, size);
        }
        List<CommunityContentSummaryDTO> items = mapSummaryList(rows);
        return new CommunityContentFeedResponse(items, offset + items.size());
    }

    public CommunityContentDetailResponse detail(Long userId, Long contentId) {
        ContentDO contentDO = contentMapper.findById(contentId);
        if (contentDO == null || "REMOVED".equals(contentDO.getStatus())) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        if (!"PUBLISHED".equals(contentDO.getStatus()) && !Objects.equals(contentDO.getAuthorId(), userId)) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        CommunityContentDetailResponse response = new CommunityContentDetailResponse();
        response.setContentId(contentDO.getId());
        response.setWorkId(contentDO.getWorkId());
        response.setTitle(contentDO.getTitle());
        response.setDescription(contentDO.getDescription());
        response.setMediaType(contentDO.getMediaType());
        response.setCoverUrl(contentDO.getCoverUrl());
        response.setMediaUrl(contentDO.getMediaUrl());
        response.setLikeCount(contentDO.getLikeCount());
        response.setFavoriteCount(contentDO.getFavoriteCount());
        response.setCommentCount(contentDO.getCommentCount());
        response.setPublishTime(contentDO.getPublishTime());
        response.setAuthor(authorOf(contentDO.getAuthorId()));
        response.setViewerState(new CommunityViewerStateDTO(
                contentLikeMapper.exists(contentId, userId),
                contentFavoriteMapper.exists(contentId, userId)
        ));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityToggleResponse toggleLike(Long userId, Long contentId) {
        ContentDO contentDO = checkPublished(contentId);
        CommunityToggleResponse response = new CommunityToggleResponse();
        try {
            contentLikeMapper.insert(contentId, userId);
            contentMapper.updateCounters(contentId, 1, 0, 0, 2);
            redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), 2D);
            response.setLiked(true);
        } catch (DuplicateKeyException ex) {
            int removed = contentLikeMapper.delete(contentId, userId);
            if (removed > 0) {
                contentMapper.updateCounters(contentId, -1, 0, 0, -2);
                redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), -2D);
            }
            response.setLiked(false);
        }
        ContentDO latest = contentMapper.findById(contentDO.getId());
        response.setLikeCount(latest == null ? 0 : latest.getLikeCount());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityToggleResponse toggleFavorite(Long userId, Long contentId) {
        ContentDO contentDO = checkPublished(contentId);
        CommunityToggleResponse response = new CommunityToggleResponse();
        try {
            contentFavoriteMapper.insert(contentId, userId);
            contentMapper.updateCounters(contentId, 0, 1, 0, 3);
            redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), 3D);
            response.setFavorited(true);
        } catch (DuplicateKeyException ex) {
            int removed = contentFavoriteMapper.delete(contentId, userId);
            if (removed > 0) {
                contentMapper.updateCounters(contentId, 0, -1, 0, -3);
                redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), -3D);
            }
            response.setFavorited(false);
        }
        ContentDO latest = contentMapper.findById(contentDO.getId());
        response.setFavoriteCount(latest == null ? 0 : latest.getFavoriteCount());
        return response;
    }

    public CommunityCommentListResponse listComments(Long contentId, Long cursor, Integer limit) {
        checkContentExists(contentId);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentCommentDO> rows = contentCommentMapper.listByContent(contentId, offset, size);
        List<CommunityCommentDTO> items = new ArrayList<>();
        for (ContentCommentDO row : rows) {
            CommunityCommentDTO dto = new CommunityCommentDTO();
            dto.setCommentId(row.getId());
            dto.setText(row.getText());
            dto.setCreatedAt(row.getCreatedAt());
            dto.setUser(authorOf(row.getUserId()));
            items.add(dto);
        }
        return new CommunityCommentListResponse(items, offset + items.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityCreateCommentResponse createComment(Long userId, Long contentId, CommunityCreateCommentRequest request) {
        checkPublished(contentId);
        sensitiveWordFilter.validate(request.getText());
        ContentCommentDO commentDO = new ContentCommentDO();
        commentDO.setContentId(contentId);
        commentDO.setUserId(userId);
        commentDO.setText(request.getText().trim());
        commentDO.setStatus("NORMAL");
        contentCommentMapper.insert(commentDO);
        contentMapper.updateCounters(contentId, 0, 0, 1, 5);
        redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), 5D);
        ContentDO latest = contentMapper.findById(contentId);
        return new CommunityCreateCommentResponse(commentDO.getId(), latest == null ? 0 : latest.getCommentCount());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        ContentCommentDO commentDO = contentCommentMapper.findById(commentId);
        if (commentDO == null || !"NORMAL".equals(commentDO.getStatus())) {
            throw new BizException(ErrorCodes.COMMENT_NOT_FOUND, "comment not found");
        }
        ContentDO contentDO = contentMapper.findById(commentDO.getContentId());
        if (contentDO == null) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        if (!Objects.equals(commentDO.getUserId(), userId) && !Objects.equals(contentDO.getAuthorId(), userId)) {
            throw new BizException(ErrorCodes.PERMISSION_DENIED, "permission denied");
        }
        int rows = contentCommentMapper.softDelete(commentId);
        if (rows <= 0) {
            throw new BizException(ErrorCodes.COMMENT_NOT_FOUND, "comment not found");
        }
        contentMapper.updateCounters(commentDO.getContentId(), 0, 0, -1, 0);
    }

    public CommunityContentFeedResponse myFavorites(Long userId, Long cursor, Integer limit) {
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows = contentMapper.listMyFavorites(userId, offset, size);
        List<CommunityContentSummaryDTO> items = mapSummaryList(rows);
        return new CommunityContentFeedResponse(items, offset + items.size());
    }

    public CommunityContentFeedResponse myPublished(Long userId, Long cursor, Integer limit) {
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows = contentMapper.listMine(userId, offset, size);
        List<CommunityContentSummaryDTO> items = mapSummaryList(rows);
        return new CommunityContentFeedResponse(items, offset + items.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public void hide(Long userId, Long contentId) {
        int rows = contentMapper.hideByAuthor(contentId, userId);
        if (rows <= 0) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        redisTemplate.opsForZSet().remove(HOT_ZSET_KEY, String.valueOf(contentId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long contentId) {
        int rows = contentMapper.removeByAuthor(contentId, userId);
        if (rows <= 0) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        redisTemplate.opsForZSet().remove(HOT_ZSET_KEY, String.valueOf(contentId));
    }

    private List<ContentDO> listHot(long offset, int limit) {
        Set<String> members = redisTemplate.opsForZSet().reverseRange(HOT_ZSET_KEY, offset, offset + limit - 1);
        LinkedHashMap<Long, ContentDO> merged = new LinkedHashMap<>();
        if (members != null) {
            for (String member : members) {
                try {
                    Long contentId = Long.parseLong(member);
                    ContentDO contentDO = contentMapper.findPublishedById(contentId);
                    if (contentDO != null) {
                        merged.put(contentId, contentDO);
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }
        if (merged.size() < limit) {
            List<ContentDO> fallback = contentMapper.listPublishedByHot(offset, limit * 2);
            for (ContentDO row : fallback) {
                merged.putIfAbsent(row.getId(), row);
                if (merged.size() >= limit) {
                    break;
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<CommunityContentSummaryDTO> mapSummaryList(List<ContentDO> rows) {
        List<CommunityContentSummaryDTO> result = new ArrayList<>();
        for (ContentDO row : rows) {
            CommunityContentSummaryDTO dto = new CommunityContentSummaryDTO();
            dto.setContentId(row.getId());
            dto.setTitle(row.getTitle());
            dto.setCoverUrl(row.getCoverUrl());
            dto.setAuthor(authorOf(row.getAuthorId()));
            dto.setLikeCount(row.getLikeCount());
            dto.setFavoriteCount(row.getFavoriteCount());
            dto.setCommentCount(row.getCommentCount());
            dto.setPublishTime(row.getPublishTime());
            result.add(dto);
        }
        return result;
    }

    private CommunityAuthorDTO authorOf(Long userId) {
        UserDO userDO = userMapper.findById(userId);
        if (userDO == null || isBlank(userDO.getUsername())) {
            return new CommunityAuthorDTO(userId, "user-" + userId, null);
        }
        return new CommunityAuthorDTO(userId, userDO.getUsername(), null);
    }

    private ContentDO checkPublished(Long contentId) {
        ContentDO contentDO = contentMapper.findPublishedById(contentId);
        if (contentDO == null) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        return contentDO;
    }

    private void checkContentExists(Long contentId) {
        ContentDO contentDO = contentMapper.findById(contentId);
        if (contentDO == null || "REMOVED".equals(contentDO.getStatus())) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
