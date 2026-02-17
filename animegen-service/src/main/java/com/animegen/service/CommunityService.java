package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.common.enums.WorkStatus;
import com.animegen.dao.domain.*;
import com.animegen.dao.mapper.*;
import com.animegen.service.dto.*;
import com.animegen.service.ranking.RankingConstants;
import com.animegen.service.ranking.RankingService;
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
    private static final String TAG_HOT_ZSET_KEY = "tag:hot";

    private final WorkMapper workMapper;
    private final UserMapper userMapper;
    private final ContentMapper contentMapper;
    private final TagMapper tagMapper;
    private final ContentTagMapper contentTagMapper;
    private final ContentLikeMapper contentLikeMapper;
    private final ContentFavoriteMapper contentFavoriteMapper;
    private final ContentCommentMapper contentCommentMapper;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final StringRedisTemplate redisTemplate;
    private final RankingService rankingService;

    public CommunityService(WorkMapper workMapper,
                            UserMapper userMapper,
                            ContentMapper contentMapper,
                            TagMapper tagMapper,
                            ContentTagMapper contentTagMapper,
                            ContentLikeMapper contentLikeMapper,
                            ContentFavoriteMapper contentFavoriteMapper,
                            ContentCommentMapper contentCommentMapper,
                            SensitiveWordFilter sensitiveWordFilter,
                            StringRedisTemplate redisTemplate,
                            RankingService rankingService) {
        this.workMapper = workMapper;
        this.userMapper = userMapper;
        this.contentMapper = contentMapper;
        this.tagMapper = tagMapper;
        this.contentTagMapper = contentTagMapper;
        this.contentLikeMapper = contentLikeMapper;
        this.contentFavoriteMapper = contentFavoriteMapper;
        this.contentCommentMapper = contentCommentMapper;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.redisTemplate = redisTemplate;
        this.rankingService = rankingService;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityPublishContentResponse publish(Long userId, CommunityPublishContentRequest request) {
        requireInteractiveUser(userId);
        LinkedHashSet<Long> tagIds = normalizeTagIds(request.getTagIds());
        validateTagsExist(tagIds);

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
        for (Long tagId : tagIds) {
            contentTagMapper.insert(contentDO.getId(), tagId);
            tagMapper.updateContentCount(tagId, 1);
            tagMapper.updateHotScore(tagId, 1);
            redisTemplate.opsForZSet().incrementScore(TAG_HOT_ZSET_KEY, String.valueOf(tagId), 1D);
        }
        redisTemplate.opsForZSet().add(HOT_ZSET_KEY, String.valueOf(contentDO.getId()), 0.0D);
        emitRankingEvent(contentDO.getId(), RankingConstants.EVENT_CONTENT_PUBLISHED, 1L);
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

    public CommunityContentFeedResponse searchContents(String keyword, Long cursor, Integer limit) {
        String q = keyword == null ? "" : keyword.trim();
        if (q.isEmpty()) {
            return new CommunityContentFeedResponse(Collections.emptyList(), 0L);
        }
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows = contentMapper.searchPublished(q, offset, size);
        List<CommunityContentSummaryDTO> items = mapSummaryList(rows);
        return new CommunityContentFeedResponse(items, offset + items.size());
    }

    public CommunityTagListResponse listHotTags(Integer limit) {
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        LinkedHashMap<Long, TagDO> merged = new LinkedHashMap<>();
        Set<String> members = redisTemplate.opsForZSet().reverseRange(TAG_HOT_ZSET_KEY, 0, size - 1L);
        if (members != null) {
            for (String member : members) {
                try {
                    Long tagId = Long.parseLong(member);
                    TagDO tagDO = tagMapper.findActiveById(tagId);
                    if (tagDO != null) {
                        merged.put(tagId, tagDO);
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }
        if (merged.size() < size) {
            for (TagDO row : tagMapper.listHot(size * 2)) {
                merged.putIfAbsent(row.getId(), row);
                if (merged.size() >= size) {
                    break;
                }
            }
        }
        return new CommunityTagListResponse(mapTagList(new ArrayList<>(merged.values())));
    }

    public CommunityTagListResponse searchTags(String keyword, Integer limit) {
        String q = keyword == null ? "" : keyword.trim();
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        if (q.isEmpty()) {
            return new CommunityTagListResponse(Collections.emptyList());
        }
        return new CommunityTagListResponse(mapTagList(tagMapper.search(q, size)));
    }

    public CommunityTagDetailResponse tagDetail(Long tagId) {
        TagDO tagDO = requireTag(tagId);
        CommunityTagDetailResponse response = new CommunityTagDetailResponse();
        response.setTagId(tagDO.getId());
        response.setName(tagDO.getName());
        response.setDescription(tagDO.getDescription());
        response.setContentCount(tagDO.getContentCount());
        response.setHotScore(tagDO.getHotScore());
        return response;
    }

    public CommunityContentFeedResponse listTagContents(Long tagId, String tab, Long cursor, Integer limit) {
        requireTag(tagId);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows = HOT_TAB.equalsIgnoreCase(tab)
                ? contentMapper.listPublishedByTagHot(tagId, offset, size)
                : contentMapper.listPublishedByTagLatest(tagId, offset, size);
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
        requireInteractiveUser(userId);
        ContentDO contentDO = checkPublished(contentId);
        CommunityToggleResponse response = new CommunityToggleResponse();
        try {
            contentLikeMapper.insert(contentId, userId);
            contentMapper.updateCounters(contentId, 1, 0, 0, 2);
            redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), 2D);
            boostTagHotByContent(contentId, 2);
            emitRankingEvent(contentId, RankingConstants.EVENT_LIKE_CREATED, 2L);
            response.setLiked(true);
        } catch (DuplicateKeyException ex) {
            int removed = contentLikeMapper.delete(contentId, userId);
            if (removed > 0) {
                contentMapper.updateCounters(contentId, -1, 0, 0, -2);
                redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), -2D);
                boostTagHotByContent(contentId, -2);
                emitRankingEvent(contentId, RankingConstants.EVENT_LIKE_CANCELED, -2L);
            }
            response.setLiked(false);
        }
        ContentDO latest = contentMapper.findById(contentDO.getId());
        response.setLikeCount(latest == null ? 0 : latest.getLikeCount());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommunityToggleResponse toggleFavorite(Long userId, Long contentId) {
        requireInteractiveUser(userId);
        ContentDO contentDO = checkPublished(contentId);
        CommunityToggleResponse response = new CommunityToggleResponse();
        try {
            contentFavoriteMapper.insert(contentId, userId);
            contentMapper.updateCounters(contentId, 0, 1, 0, 3);
            redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), 3D);
            boostTagHotByContent(contentId, 3);
            emitRankingEvent(contentId, RankingConstants.EVENT_FAVORITE_CREATED, 3L);
            response.setFavorited(true);
        } catch (DuplicateKeyException ex) {
            int removed = contentFavoriteMapper.delete(contentId, userId);
            if (removed > 0) {
                contentMapper.updateCounters(contentId, 0, -1, 0, -3);
                redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(contentId), -3D);
                boostTagHotByContent(contentId, -3);
                emitRankingEvent(contentId, RankingConstants.EVENT_FAVORITE_CANCELED, -3L);
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
        requireInteractiveUser(userId);
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
        boostTagHotByContent(contentId, 5);
        emitRankingEvent(contentId, RankingConstants.EVENT_COMMENT_CREATED, 5L);
        ContentDO latest = contentMapper.findById(contentId);
        return new CommunityCreateCommentResponse(commentDO.getId(), latest == null ? 0 : latest.getCommentCount());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        requireInteractiveUser(userId);
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
        contentMapper.updateCounters(commentDO.getContentId(), 0, 0, -1, -5);
        redisTemplate.opsForZSet().incrementScore(HOT_ZSET_KEY, String.valueOf(commentDO.getContentId()), -5D);
        boostTagHotByContent(commentDO.getContentId(), -5);
        emitRankingEvent(commentDO.getContentId(), RankingConstants.EVENT_COMMENT_DELETED, -5L);
    }

    public CommunityContentFeedResponse myFavorites(Long userId, Long cursor, Integer limit) {
        requireInteractiveUser(userId);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows = contentMapper.listMyFavorites(userId, offset, size);
        List<CommunityContentSummaryDTO> items = mapSummaryList(rows);
        return new CommunityContentFeedResponse(items, offset + items.size());
    }

    public CommunityContentFeedResponse myPublished(Long userId, Long cursor, Integer limit) {
        requireInteractiveUser(userId);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<ContentDO> rows = contentMapper.listMine(userId, offset, size);
        List<CommunityContentSummaryDTO> items = mapSummaryList(rows);
        return new CommunityContentFeedResponse(items, offset + items.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public void hide(Long userId, Long contentId) {
        requireInteractiveUser(userId);
        ContentDO before = contentMapper.findByAuthor(userId, contentId);
        if (before == null) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        int rows = contentMapper.hideByAuthor(contentId, userId);
        if (rows <= 0) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        if ("PUBLISHED".equals(before.getStatus())) {
            decrementTagContentCountByContent(contentId);
        }
        redisTemplate.opsForZSet().remove(HOT_ZSET_KEY, String.valueOf(contentId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long contentId) {
        requireInteractiveUser(userId);
        ContentDO before = contentMapper.findByAuthor(userId, contentId);
        if (before == null) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        int rows = contentMapper.removeByAuthor(contentId, userId);
        if (rows <= 0) {
            throw new BizException(ErrorCodes.CONTENT_NOT_FOUND, "content not found");
        }
        if (!"REMOVED".equals(before.getStatus())) {
            decrementTagContentCountByContent(contentId);
        }
        contentTagMapper.deleteByContentId(contentId);
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

    private List<CommunityTagDTO> mapTagList(List<TagDO> rows) {
        List<CommunityTagDTO> result = new ArrayList<>();
        for (TagDO row : rows) {
            result.add(new CommunityTagDTO(
                    row.getId(),
                    row.getName(),
                    row.getContentCount(),
                    row.getHotScore()
            ));
        }
        return result;
    }

    private LinkedHashSet<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(tagIds);
    }

    private void validateTagsExist(Set<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<TagDO> rows = tagMapper.listActiveByIds(new ArrayList<>(tagIds));
        if (rows.size() != tagIds.size()) {
            throw new BizException(ErrorCodes.TAG_NOT_FOUND, "tag not found");
        }
    }

    private TagDO requireTag(Long tagId) {
        TagDO tagDO = tagMapper.findActiveById(tagId);
        if (tagDO == null) {
            throw new BizException(ErrorCodes.TAG_NOT_FOUND, "tag not found");
        }
        return tagDO;
    }

    private void boostTagHotByContent(Long contentId, long delta) {
        if (delta == 0) {
            return;
        }
        for (Long tagId : contentTagMapper.listTagIdsByContentId(contentId)) {
            tagMapper.updateHotScore(tagId, delta);
            redisTemplate.opsForZSet().incrementScore(TAG_HOT_ZSET_KEY, String.valueOf(tagId), (double) delta);
        }
    }

    private void decrementTagContentCountByContent(Long contentId) {
        for (Long tagId : contentTagMapper.listTagIdsByContentId(contentId)) {
            tagMapper.updateContentCount(tagId, -1);
        }
    }

    private CommunityAuthorDTO authorOf(Long userId) {
        UserDO userDO = userMapper.findById(userId);
        if (userDO == null) {
            return new CommunityAuthorDTO(userId, "user-" + userId, null);
        }
        String nickname = !isBlank(userDO.getNickname())
                ? userDO.getNickname()
                : (!isBlank(userDO.getUsername()) ? userDO.getUsername() : "user-" + userId);
        return new CommunityAuthorDTO(userId, nickname, userDO.getAvatarUrl());
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

    private void requireInteractiveUser(Long userId) {
        UserDO userDO = userMapper.findById(userId);
        if (userDO == null || "GUEST".equalsIgnoreCase(userDO.getRole())) {
            throw new BizException(ErrorCodes.LOGIN_REQUIRED, "login required");
        }
    }

    private void emitRankingEvent(Long contentId, String eventType, Long scoreDelta) {
        rankingService.enqueueEvent(eventType, contentId, rankingService.buildPayload(contentId, scoreDelta));
    }
}
