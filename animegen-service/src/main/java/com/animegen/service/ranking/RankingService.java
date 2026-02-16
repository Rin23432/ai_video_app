package com.animegen.service.ranking;

import com.animegen.dao.domain.ContentDO;
import com.animegen.dao.domain.OutboxEventDO;
import com.animegen.dao.domain.RankingSnapshotDO;
import com.animegen.dao.domain.TagDO;
import com.animegen.dao.domain.UserDO;
import com.animegen.dao.mapper.*;
import com.animegen.service.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
public class RankingService {
    private final StringRedisTemplate redisTemplate;
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final ContentTagMapper contentTagMapper;
    private final OutboxEventMapper outboxEventMapper;
    private final RankingSnapshotMapper rankingSnapshotMapper;
    private final ObjectMapper objectMapper;

    public RankingService(StringRedisTemplate redisTemplate,
                          ContentMapper contentMapper,
                          UserMapper userMapper,
                          TagMapper tagMapper,
                          ContentTagMapper contentTagMapper,
                          OutboxEventMapper outboxEventMapper,
                          RankingSnapshotMapper rankingSnapshotMapper,
                          ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.tagMapper = tagMapper;
        this.contentTagMapper = contentTagMapper;
        this.outboxEventMapper = outboxEventMapper;
        this.rankingSnapshotMapper = rankingSnapshotMapper;
        this.objectMapper = objectMapper;
    }

    public void enqueueEvent(String eventType, Long aggregateId, CommunityRankingEventPayload payload) {
        OutboxEventDO eventDO = new OutboxEventDO();
        eventDO.setEventType(eventType);
        eventDO.setAggregateId(aggregateId);
        try {
            eventDO.setPayloadJson(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("serialize ranking event failed", e);
        }
        outboxEventMapper.insert(eventDO);
    }

    public CommunityRankingContentResponse listContentRankings(String window, Long cursor, Integer limit) {
        String normalizedWindow = RankingConstants.normalizeWindow(window);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        String key = RankingConstants.zsetContent(normalizedWindow);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, offset, offset + size - 1);

        List<CommunityRankingContentItemDTO> items = new ArrayList<>();
        if (tuples != null && !tuples.isEmpty()) {
            List<Long> contentIds = new ArrayList<>();
            Map<Long, Double> scoreMap = new HashMap<>();
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple == null || tuple.getValue() == null) {
                    continue;
                }
                try {
                    Long contentId = Long.parseLong(tuple.getValue());
                    contentIds.add(contentId);
                    scoreMap.put(contentId, tuple.getScore() == null ? 0D : tuple.getScore());
                } catch (NumberFormatException ignore) {
                }
            }
            Map<Long, Double> deltaBase = loadPreviousScores(RankingConstants.RANK_CONTENT, normalizedWindow, contentIds);
            int rank = (int) offset + 1;
            for (Long contentId : contentIds) {
                ContentDO contentDO = contentMapper.findPublishedById(contentId);
                if (contentDO == null) {
                    continue;
                }
                CommunityRankingContentItemDTO item = new CommunityRankingContentItemDTO();
                item.setRank(rank++);
                item.setContentId(contentDO.getId());
                item.setTitle(contentDO.getTitle());
                item.setCoverUrl(contentDO.getCoverUrl());
                item.setAuthor(authorOf(contentDO.getAuthorId()));
                Double score = scoreMap.getOrDefault(contentDO.getId(), 0D);
                item.setScore(score);
                item.setDeltaScore(score - deltaBase.getOrDefault(contentDO.getId(), 0D));
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            List<ContentDO> fallback = contentMapper.listPublishedByHot(offset, size);
            Map<Long, Double> deltaBase = loadPreviousScores(RankingConstants.RANK_CONTENT, normalizedWindow,
                    mapContentIds(fallback));
            int rank = (int) offset + 1;
            for (ContentDO row : fallback) {
                CommunityRankingContentItemDTO item = new CommunityRankingContentItemDTO();
                item.setRank(rank++);
                item.setContentId(row.getId());
                item.setTitle(row.getTitle());
                item.setCoverUrl(row.getCoverUrl());
                item.setAuthor(authorOf(row.getAuthorId()));
                double score = row.getHotScore() == null ? 0D : row.getHotScore();
                item.setScore(score);
                item.setDeltaScore(score - deltaBase.getOrDefault(row.getId(), 0D));
                items.add(item);
            }
        }
        return new CommunityRankingContentResponse(normalizedWindow, items, offset + items.size());
    }

    public CommunityRankingAuthorResponse listAuthorRankings(String window, Long cursor, Integer limit) {
        String normalizedWindow = RankingConstants.normalizeWindow(window);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        String key = RankingConstants.zsetAuthor(normalizedWindow);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, offset, offset + size - 1);
        List<CommunityRankingAuthorItemDTO> items = new ArrayList<>();

        if (tuples != null && !tuples.isEmpty()) {
            List<Long> authorIds = new ArrayList<>();
            Map<Long, Double> scoreMap = new HashMap<>();
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple == null || tuple.getValue() == null) {
                    continue;
                }
                try {
                    Long authorId = Long.parseLong(tuple.getValue());
                    authorIds.add(authorId);
                    scoreMap.put(authorId, tuple.getScore() == null ? 0D : tuple.getScore());
                } catch (NumberFormatException ignore) {
                }
            }
            Map<Long, Double> deltaBase = loadPreviousScores(RankingConstants.RANK_AUTHOR, normalizedWindow, authorIds);
            int rank = (int) offset + 1;
            for (Long authorId : authorIds) {
                CommunityRankingAuthorItemDTO item = buildAuthorRankingItem(authorId, rank++, scoreMap.getOrDefault(authorId, 0D));
                item.setDeltaScore(item.getScore() - deltaBase.getOrDefault(authorId, 0D));
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            Map<Long, Double> fallbackMap = new LinkedHashMap<>();
            List<ContentDO> fallback = contentMapper.listPublishedByHot(0L, size * 5);
            for (ContentDO row : fallback) {
                if (row.getAuthorId() == null) {
                    continue;
                }
                double delta = row.getHotScore() == null ? 0D : row.getHotScore();
                fallbackMap.put(row.getAuthorId(), fallbackMap.getOrDefault(row.getAuthorId(), 0D) + delta);
            }
            List<Map.Entry<Long, Double>> sorted = new ArrayList<>(fallbackMap.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            List<Long> authorIds = new ArrayList<>();
            for (int i = (int) offset; i < sorted.size() && authorIds.size() < size; i++) {
                authorIds.add(sorted.get(i).getKey());
            }
            Map<Long, Double> deltaBase = loadPreviousScores(RankingConstants.RANK_AUTHOR, normalizedWindow, authorIds);
            int rank = (int) offset + 1;
            for (Long authorId : authorIds) {
                Double score = fallbackMap.getOrDefault(authorId, 0D);
                CommunityRankingAuthorItemDTO item = buildAuthorRankingItem(authorId, rank++, score);
                item.setDeltaScore(score - deltaBase.getOrDefault(authorId, 0D));
                items.add(item);
            }
        }
        return new CommunityRankingAuthorResponse(normalizedWindow, items, offset + items.size());
    }

    public CommunityRankingTagResponse listTagRankings(String window, Long cursor, Integer limit) {
        String normalizedWindow = RankingConstants.normalizeWindow(window);
        long offset = cursor == null ? 0L : Math.max(cursor, 0L);
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        String key = RankingConstants.zsetTag(normalizedWindow);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, offset, offset + size - 1);
        List<CommunityRankingTagItemDTO> items = new ArrayList<>();

        if (tuples != null && !tuples.isEmpty()) {
            List<Long> tagIds = new ArrayList<>();
            Map<Long, Double> scoreMap = new HashMap<>();
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                if (tuple == null || tuple.getValue() == null) {
                    continue;
                }
                try {
                    Long tagId = Long.parseLong(tuple.getValue());
                    tagIds.add(tagId);
                    scoreMap.put(tagId, tuple.getScore() == null ? 0D : tuple.getScore());
                } catch (NumberFormatException ignore) {
                }
            }
            Map<Long, Double> deltaBase = loadPreviousScores(RankingConstants.RANK_TAG, normalizedWindow, tagIds);
            int rank = (int) offset + 1;
            for (Long tagId : tagIds) {
                TagDO tagDO = tagMapper.findActiveById(tagId);
                if (tagDO == null) {
                    continue;
                }
                CommunityRankingTagItemDTO item = new CommunityRankingTagItemDTO();
                item.setRank(rank++);
                item.setTagId(tagDO.getId());
                item.setName(tagDO.getName());
                item.setContentCount(tagDO.getContentCount());
                Double score = scoreMap.getOrDefault(tagDO.getId(), 0D);
                item.setScore(score);
                item.setDeltaScore(score - deltaBase.getOrDefault(tagDO.getId(), 0D));
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            List<TagDO> fallback = tagMapper.listHot(size);
            Map<Long, Double> deltaBase = loadPreviousScores(RankingConstants.RANK_TAG, normalizedWindow, mapTagIds(fallback));
            int rank = (int) offset + 1;
            for (TagDO row : fallback) {
                CommunityRankingTagItemDTO item = new CommunityRankingTagItemDTO();
                item.setRank(rank++);
                item.setTagId(row.getId());
                item.setName(row.getName());
                item.setContentCount(row.getContentCount());
                double score = row.getHotScore() == null ? 0D : row.getHotScore();
                item.setScore(score);
                item.setDeltaScore(score - deltaBase.getOrDefault(row.getId(), 0D));
                items.add(item);
            }
        }
        return new CommunityRankingTagResponse(normalizedWindow, items, offset + items.size());
    }

    public void processOutboxEvent(OutboxEventDO eventDO) {
        CommunityRankingEventPayload payload;
        try {
            payload = objectMapper.readValue(eventDO.getPayloadJson(), CommunityRankingEventPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("parse ranking payload failed", e);
        }
        if (payload.getScoreDelta() == null || payload.getScoreDelta() == 0) {
            return;
        }
        long delta = payload.getScoreDelta();
        for (String window : Arrays.asList(RankingConstants.WINDOW_DAILY, RankingConstants.WINDOW_WEEKLY, RankingConstants.WINDOW_MONTHLY)) {
            if (payload.getContentId() != null) {
                String key = RankingConstants.zsetContent(window);
                redisTemplate.opsForZSet().incrementScore(key, String.valueOf(payload.getContentId()), (double) delta);
                applyTtl(key, window);
            }
            if (payload.getAuthorId() != null) {
                String key = RankingConstants.zsetAuthor(window);
                redisTemplate.opsForZSet().incrementScore(key, String.valueOf(payload.getAuthorId()), (double) delta);
                applyTtl(key, window);
            }
            if (payload.getTagIds() != null) {
                String key = RankingConstants.zsetTag(window);
                for (Long tagId : payload.getTagIds()) {
                    redisTemplate.opsForZSet().incrementScore(key, String.valueOf(tagId), (double) delta);
                }
                applyTtl(key, window);
            }
        }
    }

    public void snapshotAll(String window) {
        String normalizedWindow = RankingConstants.normalizeWindow(window);
        snapshotRankType(RankingConstants.RANK_CONTENT, normalizedWindow, RankingConstants.zsetContent(normalizedWindow));
        snapshotRankType(RankingConstants.RANK_AUTHOR, normalizedWindow, RankingConstants.zsetAuthor(normalizedWindow));
        snapshotRankType(RankingConstants.RANK_TAG, normalizedWindow, RankingConstants.zsetTag(normalizedWindow));
    }

    private void snapshotRankType(String rankType, String window, String key) {
        String bizDate = LocalDate.now().toString();
        rankingSnapshotMapper.deleteByDay(rankType, window, bizDate);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 99);
        if (tuples == null || tuples.isEmpty()) {
            return;
        }
        int rankNo = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple == null || tuple.getValue() == null) {
                continue;
            }
            Long entityId;
            try {
                entityId = Long.parseLong(tuple.getValue());
            } catch (NumberFormatException ex) {
                continue;
            }
            RankingSnapshotDO snapshotDO = new RankingSnapshotDO();
            snapshotDO.setRankType(rankType);
            snapshotDO.setWindow(window);
            snapshotDO.setBizDate(LocalDate.now());
            snapshotDO.setRankNo(rankNo++);
            snapshotDO.setEntityId(entityId);
            snapshotDO.setScore(tuple.getScore() == null ? 0D : tuple.getScore());
            snapshotDO.setMetaJson(null);
            rankingSnapshotMapper.insert(snapshotDO);
        }
    }

    private void applyTtl(String key, String window) {
        Duration ttl;
        if (RankingConstants.WINDOW_DAILY.equals(window)) {
            ttl = Duration.ofDays(2);
        } else if (RankingConstants.WINDOW_WEEKLY.equals(window)) {
            ttl = Duration.ofDays(10);
        } else {
            ttl = Duration.ofDays(40);
        }
        redisTemplate.expire(key, ttl);
    }

    private CommunityRankingAuthorItemDTO buildAuthorRankingItem(Long authorId, int rank, Double score) {
        CommunityRankingAuthorItemDTO item = new CommunityRankingAuthorItemDTO();
        item.setRank(rank);
        item.setAuthor(authorOf(authorId));
        item.setPublishedCount(contentMapper.countPublishedByAuthor(authorId));
        item.setLikesReceived(contentMapper.sumLikesReceived(authorId));
        item.setScore(score == null ? 0D : score);
        item.setDeltaScore(0D);
        return item;
    }

    private CommunityAuthorDTO authorOf(Long userId) {
        UserDO userDO = userMapper.findById(userId);
        if (userDO == null) {
            return new CommunityAuthorDTO(userId, "user-" + userId, null);
        }
        String nickname = isBlank(userDO.getNickname())
                ? (isBlank(userDO.getUsername()) ? "user-" + userId : userDO.getUsername())
                : userDO.getNickname();
        return new CommunityAuthorDTO(userId, nickname, userDO.getAvatarUrl());
    }

    private Map<Long, Double> loadPreviousScores(String rankType, String window, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<RankingSnapshotDO> rows = rankingSnapshotMapper.listByEntities(rankType, window,
                LocalDate.now().minusDays(1).toString(), entityIds);
        Map<Long, Double> result = new HashMap<>();
        for (RankingSnapshotDO row : rows) {
            result.put(row.getEntityId(), row.getScore() == null ? 0D : row.getScore());
        }
        return result;
    }

    private static List<Long> mapContentIds(List<ContentDO> rows) {
        List<Long> ids = new ArrayList<>();
        for (ContentDO row : rows) {
            ids.add(row.getId());
        }
        return ids;
    }

    private static List<Long> mapTagIds(List<TagDO> rows) {
        List<Long> ids = new ArrayList<>();
        for (TagDO row : rows) {
            ids.add(row.getId());
        }
        return ids;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public CommunityRankingEventPayload buildPayload(Long contentId, Long scoreDelta) {
        CommunityRankingEventPayload payload = new CommunityRankingEventPayload();
        payload.setContentId(contentId);
        payload.setScoreDelta(scoreDelta);
        ContentDO contentDO = contentMapper.findById(contentId);
        if (contentDO != null) {
            payload.setAuthorId(contentDO.getAuthorId());
            payload.setTagIds(contentTagMapper.listTagIdsByContentId(contentId));
        }
        return payload;
    }
}
