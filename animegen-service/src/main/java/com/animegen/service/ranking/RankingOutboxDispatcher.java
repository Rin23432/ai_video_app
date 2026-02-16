package com.animegen.service.ranking;

import com.animegen.dao.domain.OutboxEventDO;
import com.animegen.dao.mapper.OutboxEventMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RankingOutboxDispatcher {
    private final OutboxEventMapper outboxEventMapper;
    private final RankingService rankingService;

    public RankingOutboxDispatcher(OutboxEventMapper outboxEventMapper, RankingService rankingService) {
        this.outboxEventMapper = outboxEventMapper;
        this.rankingService = rankingService;
    }

    @Scheduled(fixedDelay = 3000L)
    public void dispatch() {
        List<OutboxEventDO> pending = outboxEventMapper.listPending(50);
        for (OutboxEventDO eventDO : pending) {
            if (outboxEventMapper.markProcessing(eventDO.getId()) <= 0) {
                continue;
            }
            try {
                rankingService.processOutboxEvent(eventDO);
                outboxEventMapper.markDone(eventDO.getId());
            } catch (Exception ex) {
                int retry = eventDO.getRetryCount() == null ? 0 : eventDO.getRetryCount();
                if (retry >= 3) {
                    outboxEventMapper.markFailed(eventDO.getId(), truncate(ex.getMessage(), 500));
                } else {
                    outboxEventMapper.markRetry(eventDO.getId(), 15, truncate(ex.getMessage(), 500));
                }
            }
        }
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
