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
