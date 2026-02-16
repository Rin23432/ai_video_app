package com.animegen.service.ranking;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankingSnapshotJob {
    private final RankingService rankingService;

    public RankingSnapshotJob(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Scheduled(fixedDelay = 600000L, initialDelay = 60000L)
    public void refreshSnapshots() {
        rankingService.snapshotAll(RankingConstants.WINDOW_DAILY);
        rankingService.snapshotAll(RankingConstants.WINDOW_WEEKLY);
        rankingService.snapshotAll(RankingConstants.WINDOW_MONTHLY);
    }
}
