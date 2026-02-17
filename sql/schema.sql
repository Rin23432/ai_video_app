CREATE DATABASE IF NOT EXISTS animegen DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE animegen;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NULL,
    password_hash VARCHAR(255) NULL,
    phone VARCHAR(32) NULL,
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(512) NULL,
    bio VARCHAR(256) NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'GUEST',
    device_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username(username),
    UNIQUE KEY uk_phone(phone),
    UNIQUE KEY uk_device_id(device_id),
    INDEX idx_role(role)
);

ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS phone VARCHAR(32) NULL,
    ADD COLUMN IF NOT EXISTS nickname VARCHAR(64) NOT NULL DEFAULT 'Guest',
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(512) NULL,
    ADD COLUMN IF NOT EXISTS bio VARCHAR(256) NULL,
    ADD COLUMN IF NOT EXISTS device_id VARCHAR(64) NULL;

ALTER TABLE `user`
    MODIFY COLUMN username VARCHAR(64) NULL,
    MODIFY COLUMN role VARCHAR(16) NOT NULL DEFAULT 'GUEST';


CREATE TABLE IF NOT EXISTS work (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    prompt TEXT NOT NULL,
    style_id VARCHAR(64) NOT NULL,
    aspect_ratio VARCHAR(16) NOT NULL,
    duration_sec INT NOT NULL,
    status VARCHAR(16) NOT NULL,
    cover_url VARCHAR(512) NULL,
    video_url VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_work_user_created(user_id, created_at)
);

CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    progress INT NOT NULL,
    stage VARCHAR(32) NOT NULL,
    error_code VARCHAR(64) NULL,
    error_message VARCHAR(512) NULL,
    retry_count INT NOT NULL DEFAULT 0,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_work_created(work_id, created_at),
    INDEX idx_task_status_updated(status, updated_at)
);

CREATE TABLE IF NOT EXISTS asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    asset_type VARCHAR(32) NOT NULL,
    url VARCHAR(512) NOT NULL,
    meta_json TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_asset_work(work_id),
    INDEX idx_asset_task(task_id)
);

CREATE TABLE IF NOT EXISTS content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(512) NULL,
    media_type VARCHAR(16) NOT NULL,
    cover_url VARCHAR(512) NOT NULL,
    media_url VARCHAR(512) NOT NULL,
    status VARCHAR(16) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    hot_score BIGINT NOT NULL DEFAULT 0,
    publish_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_content_work(work_id),
    INDEX idx_content_publish_time(publish_time),
    INDEX idx_content_hot_score(hot_score),
    INDEX idx_content_author(author_id, publish_time),
    INDEX idx_content_status(status, publish_time)
);

CREATE TABLE IF NOT EXISTS content_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_like(content_id, user_id),
    INDEX idx_like_user(user_id)
);

CREATE TABLE IF NOT EXISTS content_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorite(content_id, user_id),
    INDEX idx_favorite_user(user_id)
);

CREATE TABLE IF NOT EXISTS content_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    text VARCHAR(300) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_content(content_id, created_at),
    INDEX idx_comment_user(user_id, created_at)
);

CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255) NULL,
    content_count INT NOT NULL DEFAULT 0,
    hot_score BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_name(name),
    INDEX idx_tag_hot_score(hot_score),
    INDEX idx_tag_status_hot(status, hot_score)
);

CREATE TABLE IF NOT EXISTS content_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_content_tag(content_id, tag_id),
    INDEX idx_tag_content(tag_id, content_id),
    INDEX idx_content_tag(content_id, tag_id)
);

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

CREATE TABLE IF NOT EXISTS video_product_spu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_user_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    subtitle VARCHAR(255) DEFAULT NULL,
    description TEXT,
    cover_url VARCHAR(512) DEFAULT NULL,
    category VARCHAR(64) DEFAULT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_video_spu_creator_status(creator_user_id, status),
    INDEX idx_video_spu_status_updated(status, updated_at)
);

CREATE TABLE IF NOT EXISTS video_product_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spu_id BIGINT NOT NULL,
    video_work_id BIGINT NOT NULL,
    price_cent INT NOT NULL,
    origin_price_cent INT DEFAULT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
    valid_days INT DEFAULT NULL,
    stock_total INT NOT NULL DEFAULT 0,
    stock_available INT NOT NULL DEFAULT 0,
    stock_reserved INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ON_SALE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_video_sku_spu_work(spu_id, video_work_id),
    INDEX idx_video_sku_spu_status(spu_id, status),
    INDEX idx_video_sku_status_updated(status, updated_at)
);

CREATE TABLE IF NOT EXISTS video_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount_cent INT NOT NULL,
    pay_amount_cent INT NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
    status VARCHAR(32) NOT NULL,
    pay_channel VARCHAR(32) DEFAULT NULL,
    pay_deadline_at DATETIME DEFAULT NULL,
    paid_at DATETIME DEFAULT NULL,
    closed_at DATETIME DEFAULT NULL,
    version INT NOT NULL DEFAULT 0,
    request_id VARCHAR(64) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_video_order_no(order_no),
    UNIQUE KEY uk_video_order_user_request(user_id, request_id),
    INDEX idx_video_order_user_created(user_id, created_at),
    INDEX idx_video_order_status_created(status, created_at)
);

CREATE TABLE IF NOT EXISTS video_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    sku_id BIGINT NOT NULL,
    spu_id BIGINT NOT NULL,
    work_id BIGINT NOT NULL,
    title_snapshot VARCHAR(128) NOT NULL,
    cover_snapshot VARCHAR(512) DEFAULT NULL,
    unit_price_cent INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    amount_cent INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_video_order_item_order_no(order_no),
    INDEX idx_video_order_item_sku(sku_id)
);

CREATE TABLE IF NOT EXISTS video_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    channel_txn_no VARCHAR(128) DEFAULT NULL,
    status VARCHAR(32) NOT NULL,
    amount_cent INT NOT NULL,
    callback_raw TEXT,
    paid_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_video_payment_channel_txn(channel, channel_txn_no),
    INDEX idx_video_payment_order_no(order_no)
);

CREATE TABLE IF NOT EXISTS video_entitlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    work_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    source VARCHAR(32) NOT NULL DEFAULT 'PURCHASE',
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at DATETIME DEFAULT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_video_entitlement_user_work_order(user_id, work_id, order_no),
    INDEX idx_video_entitlement_user_status(user_id, status),
    INDEX idx_video_entitlement_user_expire(user_id, expire_at)
);
