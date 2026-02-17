USE animegen;

CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW',
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME NULL,
    error_message VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_outbox_pending(status, next_retry_at, id),
    INDEX idx_outbox_aggregate(aggregate_id, created_at)
);

CREATE TABLE IF NOT EXISTS ranking_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rank_type VARCHAR(16) NOT NULL,
    window VARCHAR(16) NOT NULL,
    biz_date DATE NOT NULL,
    rank_no INT NOT NULL,
    entity_id BIGINT NOT NULL,
    score DOUBLE NOT NULL DEFAULT 0,
    meta_json TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rank_snapshot(rank_type, window, biz_date, rank_no),
    INDEX idx_rank_entity(rank_type, window, biz_date, entity_id)
);
