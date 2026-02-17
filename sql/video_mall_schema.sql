-- 视频商城模块 DDL（MySQL 8）
-- 说明：独立模块建表，可与现有 schema.sql 并行维护。

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
  INDEX idx_creator_status (creator_user_id, status),
  INDEX idx_status_updated (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  UNIQUE KEY uk_spu_work (spu_id, video_work_id),
  INDEX idx_spu_status (spu_id, status),
  INDEX idx_status_updated (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  UNIQUE KEY uk_order_no (order_no),
  UNIQUE KEY uk_user_request (user_id, request_id),
  INDEX idx_user_created (user_id, created_at),
  INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  INDEX idx_order_no (order_no),
  INDEX idx_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  UNIQUE KEY uk_channel_txn (channel, channel_txn_no),
  INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  UNIQUE KEY uk_user_work_order (user_id, work_id, order_no),
  INDEX idx_user_status (user_id, status),
  INDEX idx_user_expire (user_id, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(64) NOT NULL,
  biz_key VARCHAR(128) NOT NULL,
  topic VARCHAR(128) NOT NULL,
  payload_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'NEW',
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status_retry (status, next_retry_at),
  INDEX idx_biz (biz_type, biz_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
