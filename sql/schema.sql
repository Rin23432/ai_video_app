CREATE DATABASE IF NOT EXISTS animegen DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE animegen;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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
