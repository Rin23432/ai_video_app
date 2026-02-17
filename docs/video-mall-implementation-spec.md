# 视频商城实现规格（DDL + API + Redis + MQ）

> 适配当前仓库风格：Spring Boot + Spring MVC + MyBatis + MySQL + Redis。

## 1. 数据库 DDL（MySQL 8）

```sql
-- 1) 商品SPU
CREATE TABLE IF NOT EXISTS video_product_spu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  creator_user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  subtitle VARCHAR(255) DEFAULT NULL,
  description TEXT,
  cover_url VARCHAR(512) DEFAULT NULL,
  category VARCHAR(64) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- DRAFT/ONLINE/OFFLINE
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_creator_status (creator_user_id, status),
  INDEX idx_status_updated (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 商品SKU（可做多价格版本）
CREATE TABLE IF NOT EXISTS video_product_sku (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  spu_id BIGINT NOT NULL,
  video_work_id BIGINT NOT NULL,
  price_cent INT NOT NULL,
  origin_price_cent INT DEFAULT NULL,
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  valid_days INT DEFAULT NULL, -- NULL 表示永久
  stock_total INT NOT NULL DEFAULT 0,
  stock_available INT NOT NULL DEFAULT 0,
  stock_reserved INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ON_SALE', -- ON_SALE/OFF_SHELF
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_spu_work (spu_id, video_work_id),
  INDEX idx_spu_status (spu_id, status),
  INDEX idx_status_updated (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) 订单主表
CREATE TABLE IF NOT EXISTS video_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  total_amount_cent INT NOT NULL,
  pay_amount_cent INT NOT NULL,
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  status VARCHAR(32) NOT NULL, -- INIT/PENDING_PAY/PAID/DELIVERED/CLOSED/REFUNDED
  pay_channel VARCHAR(32) DEFAULT NULL,
  pay_deadline_at DATETIME DEFAULT NULL,
  paid_at DATETIME DEFAULT NULL,
  closed_at DATETIME DEFAULT NULL,
  version INT NOT NULL DEFAULT 0,
  request_id VARCHAR(64) DEFAULT NULL, -- 幂等键
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_no (order_no),
  UNIQUE KEY uk_user_request (user_id, request_id),
  INDEX idx_user_created (user_id, created_at),
  INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) 订单明细
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

-- 5) 支付流水
CREATE TABLE IF NOT EXISTS video_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  channel_txn_no VARCHAR(128) DEFAULT NULL,
  status VARCHAR(32) NOT NULL, -- INIT/SUCCESS/FAILED
  amount_cent INT NOT NULL,
  callback_raw TEXT,
  paid_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_channel_txn (channel, channel_txn_no),
  INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) 用户权益（是否可播放）
CREATE TABLE IF NOT EXISTS video_entitlement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  work_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  source VARCHAR(32) NOT NULL DEFAULT 'PURCHASE',
  granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expire_at DATETIME DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE/REVOKED/EXPIRED
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_work_order (user_id, work_id, order_no),
  INDEX idx_user_status (user_id, status),
  INDEX idx_user_expire (user_id, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) Outbox 本地消息表
CREATE TABLE IF NOT EXISTS outbox_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(64) NOT NULL,
  biz_key VARCHAR(128) NOT NULL,
  topic VARCHAR(128) NOT NULL,
  payload_json JSON NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'NEW', -- NEW/SENT/FAILED
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status_retry (status, next_retry_at),
  INDEX idx_biz (biz_type, biz_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 2. API 规格（字段级）

统一返回（建议）：

```json
{ "code": 0, "message": "OK", "data": {} }
```

## 2.1 商品列表

`GET /api/v1/mall/videos?cursor=0&limit=20&sort=latest&category=anime`

- `sort`: `latest|hot|price_asc|price_desc`

响应 `data`：

```json
{
  "items": [
    {
      "spuId": 1001,
      "skuId": 2001,
      "workId": 3001,
      "title": "赛博武士短片",
      "coverUrl": "https://...",
      "priceCent": 990,
      "originPriceCent": 1990,
      "currency": "CNY",
      "stockAvailable": 128,
      "status": "ON_SALE"
    }
  ],
  "nextCursor": 2001
}
```

## 2.2 商品详情

`GET /api/v1/mall/videos/{skuId}`

响应 `data`：

```json
{
  "spuId": 1001,
  "skuId": 2001,
  "workId": 3001,
  "title": "赛博武士短片",
  "subtitle": "90秒高燃战斗",
  "description": "...",
  "coverUrl": "https://...",
  "priceCent": 990,
  "originPriceCent": 1990,
  "currency": "CNY",
  "validDays": null,
  "stockAvailable": 128,
  "status": "ON_SALE"
}
```

## 2.3 创建订单

`POST /api/v1/mall/orders`

请求：

```json
{
  "requestId": "uuid-xxxx",
  "items": [
    { "skuId": 2001, "quantity": 1 }
  ],
  "client": {
    "deviceId": "android-xxx",
    "ip": "x.x.x.x"
  }
}
```

响应 `data`：

```json
{
  "orderNo": "VM202602171234000001",
  "status": "PENDING_PAY",
  "payAmountCent": 990,
  "currency": "CNY",
  "payDeadlineAt": "2026-02-17T12:49:00Z"
}
```

## 2.4 预支付

`POST /api/v1/mall/payments/{orderNo}/prepay`

请求：

```json
{
  "channel": "WECHAT_JSAPI",
  "returnUrl": "animegen://mall/pay/result"
}
```

响应 `data`：

```json
{
  "channel": "WECHAT_JSAPI",
  "prepayToken": "xxx",
  "expireAt": "2026-02-17T12:49:00Z"
}
```

## 2.5 支付回调

`POST /api/v1/mall/payments/callback`

请求：

```json
{
  "channel": "WECHAT_JSAPI",
  "orderNo": "VM202602171234000001",
  "channelTxnNo": "420000xxxx",
  "status": "SUCCESS",
  "amountCent": 990,
  "paidAt": "2026-02-17T12:36:12Z",
  "raw": "..."
}
```

响应：`{"code":0,"message":"OK","data":true}`

## 2.6 我的订单

`GET /api/v1/mall/me/orders?cursor=0&limit=20&status=PAID`

响应 `data`：

```json
{
  "items": [
    {
      "orderNo": "VM202602171234000001",
      "status": "PAID",
      "payAmountCent": 990,
      "currency": "CNY",
      "createdAt": "2026-02-17T12:34:00Z",
      "paidAt": "2026-02-17T12:36:12Z"
    }
  ],
  "nextCursor": 101
}
```

## 2.7 我的权益

`GET /api/v1/mall/me/entitlements?cursor=0&limit=20`

响应 `data`：

```json
{
  "items": [
    {
      "workId": 3001,
      "title": "赛博武士短片",
      "coverUrl": "https://...",
      "grantedAt": "2026-02-17T12:36:15Z",
      "expireAt": null,
      "status": "ACTIVE"
    }
  ],
  "nextCursor": 5001
}
```

## 2.8 播放鉴权

`GET /api/v1/mall/play/auth/{workId}`

响应 `data`：

```json
{
  "allowed": true,
  "reason": "OK",
  "playToken": "jwt-or-signature",
  "playUrl": "https://cdn.example.com/xxx.m3u8?sign=...",
  "expireAt": "2026-02-17T13:00:00Z"
}
```

## 3. Redis Key 设计

统一前缀：`animegen:mall:`

- 幂等单：`order:idemp:{userId}:{requestId}` -> `orderNo`，TTL `24h`
- SKU缓存：`sku:detail:{skuId}` -> JSON，TTL `5m`
- 列表缓存：`sku:list:{category}:{sort}:{cursor}:{limit}` -> JSON，TTL `30s`
- 库存可售：`stock:avail:{skuId}` -> int
- 库存预留：`stock:reserved:{skuId}` -> int
- 限购：`limit:user:{userId}:{skuId}` -> int，TTL `1d`
- 下单限流：`rl:order:{userId}` -> sliding window
- 回调幂等：`pay:callback:{channel}:{channelTxnNo}` -> 1，TTL `7d`

Lua（预扣库存）建议：

- 输入：`skuId, delta`
- 逻辑：`if avail >= delta then avail -= delta; reserved += delta; return 1 else return 0`

## 4. MQ Topic 设计

建议 Topic：

- `mall.order.created`
- `mall.order.pay.timeout`
- `mall.payment.success`
- `mall.entitlement.grant`
- `mall.entitlement.revoke`
- `mall.order.closed`
- `mall.outbox.retry`

消息字段统一：

```json
{
  "eventId": "uuid",
  "eventType": "mall.payment.success",
  "bizKey": "VM202602171234000001",
  "occurredAt": "2026-02-17T12:36:12Z",
  "traceId": "...",
  "payload": {}
}
```

幂等消费建议：

- Redis setnx 或 DB 去重表记录 `eventId`
- 消费失败重试 + 死信队列
- 消费处理使用“状态前置校验”（例如仅 `PENDING_PAY -> PAID`）

## 5. 服务分层建议（贴近当前项目）

- `animegen-api`
  - `MallCatalogController`
  - `MallOrderController`
  - `MallPaymentController`
  - `MallEntitlementController`
- `animegen-service`
  - `MallCatalogService`
  - `MallOrderService`
  - `MallPaymentService`
  - `MallEntitlementService`
- `animegen-dao`
  - `VideoProductSpuMapper`
  - `VideoProductSkuMapper`
  - `VideoOrderMapper`
  - `VideoOrderItemMapper`
  - `VideoPaymentMapper`
  - `VideoEntitlementMapper`
  - `OutboxEventMapper`

## 6. 面试高频问题可答点

- 防超卖：Redis Lua 原子预扣 + DB 条件更新双保险
- 一致性：本地事务 + Outbox + MQ 最终一致
- 幂等：创建订单 `requestId`、支付回调 `channelTxnNo`
- 高并发：网关限流 + 热点缓存 + 延迟关单 + 异步化发放
- 可观测：下单成功率/支付成功率/库存一致率/死信堆积量

---

如需下一步，我可以继续生成：

1. `sql/video_mall_schema.sql`（可直接执行）
2. `api.http` 的商城接口请求样例
3. `Mapper.xml + Service 接口骨架` 的代码模板
