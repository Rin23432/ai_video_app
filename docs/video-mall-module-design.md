# 视频商城模块设计（独立模块）

## 1. 模块定位

视频商城（`video-mall`）是一个独立业务模块，目标是支持“视频商品化售卖 + 购买 + 权益发放 + 播放授权”。

- 面向用户：浏览视频商品、下单支付、查看已购、播放已购内容
- 面向创作者/运营：上架视频、定价、活动、库存与销售数据
- 面向系统：高并发抗压、交易一致性、风控与可观测

## 2. 边界与职责

- `mall-catalog`：商品与定价（SPU/SKU）
- `mall-order`：订单创建、状态流转、取消/关闭
- `mall-payment`：支付下单、回调处理、支付对账
- `mall-entitlement`：权益发放（用户购买后可播放）
- `mall-inventory`：库存扣减与回补（支持活动库存）
- `mall-search`：商品检索与排序（ES 可选）

## 3. 大厂后端偏好技术栈（面试友好）

## 3.1 核心栈

- Java 17
- Spring Boot + Spring MVC + Spring Validation
- MyBatis / MyBatis-Plus
- MySQL 8（InnoDB，读写分离）
- Redis（缓存、分布式锁、限流、库存预扣）
- Kafka / RocketMQ（异步下单、延迟关单、事件驱动）
- Nginx + Gateway（鉴权、限流、路由）

## 3.2 工程与稳定性

- Sentinel / Resilience4j（限流、熔断、降级）
- Seata（可选，跨服务事务）
- Redisson（分布式锁）
- Prometheus + Grafana + Loki + OpenTelemetry（观测）
- Docker + K8s（弹性扩容）

## 3.3 视频存储与分发

- MinIO / OSS / S3（视频文件与封面）
- CDN（热点视频分发）
- URL 鉴权（防盗链，时效签名）

## 4. 核心数据模型

- `video_product_spu`：视频商品主体（标题、简介、封面、状态）
- `video_product_sku`：价格版本（售价、原价、币种、有效期）
- `video_inventory`：总库存/活动库存/冻结库存
- `video_order`：订单主表（状态机）
- `video_order_item`：订单明细
- `video_payment`：支付记录（渠道单号、支付状态）
- `video_entitlement`：用户权益（购买后可播放）
- `outbox_event`：本地消息表（保证事件可靠投递）

## 5. 关键接口（示例）

- `GET /api/v1/mall/videos`：商品列表（支持分页、排序、筛选）
- `GET /api/v1/mall/videos/{skuId}`：商品详情
- `POST /api/v1/mall/orders`：创建订单（幂等）
- `POST /api/v1/mall/payments/{orderNo}/prepay`：拉起支付
- `POST /api/v1/mall/payments/callback`：支付回调
- `GET /api/v1/mall/me/orders`：我的订单
- `GET /api/v1/mall/me/entitlements`：我的已购
- `GET /api/v1/mall/play/auth/{videoId}`：播放授权校验

## 6. 高并发设计（核心）

## 6.1 下单链路（抗压）

1. 网关限流 + 黑白名单 + 设备指纹校验
2. Redis 校验幂等令牌（防重复提交）
3. Redis Lua 原子预扣库存（防超卖）
4. 写入订单“待支付”状态
5. 投递 MQ 事件（后续异步处理）

## 6.2 防超卖方案

- 预扣库存：`available - reserved`，Lua 保证原子
- DB 最终扣减：`update ... where available >= x`
- 扣减失败触发补偿：回补 Redis 预扣
- 活动场景可加“用户限购键”防黄牛

## 6.3 延迟关单

- 订单创建后发送延迟消息（例如 15 分钟）
- 到期未支付则自动关单并回补库存
- 通过“状态机 + 版本号”保证幂等关闭

## 6.4 缓存架构

- 商品详情：Redis + 本地热点缓存（Caffeine）
- 防击穿：互斥锁 / 逻辑过期
- 防穿透：布隆过滤器 + 空值缓存
- 防雪崩：过期时间随机抖动

## 6.5 一致性策略

- 交易主链路：本地事务优先，避免分布式强一致长事务
- 异步一致性：Outbox + MQ + 重试 + 死信队列
- 支付回调：按 `orderNo + channelTxnNo` 做幂等落库

## 7. 订单状态机（建议）

- `INIT`：初始化
- `PENDING_PAY`：待支付
- `PAID`：已支付
- `DELIVERED`：权益已发放
- `CLOSED`：超时关闭
- `REFUNDED`：已退款

状态迁移必须带“当前状态校验 + version 乐观锁”，防并发写乱序。

## 8. 权益发放与播放鉴权

- 支付成功后异步发放 `video_entitlement`
- 播放时校验：用户是否拥有权益 + 是否过期 + 设备策略
- 生成短时播放 token（JWT / 签名 URL）
- CDN 回源校验 token，防盗播与链路外泄

## 9. 风控与安全

- 接口签名 + 时间戳 + nonce（防重放）
- 用户/设备/IP 多维限频
- 异常行为检测：短时高频下单、批量小额支付失败
- 管理端二次审核（高风险视频下架）

## 10. 可观测与SLA

关键指标（必须上报）：

- QPS、P99 延迟、错误率
- 下单成功率、支付成功率、发放成功率
- 库存一致率（Redis 与 DB 偏差）
- 回调幂等命中率、死信堆积量

建议目标：

- 核心下单接口可用性 >= 99.95%
- 支付回调处理时延 P99 < 1s
- 权益发放最终一致性 < 30s

## 11. 数据库设计建议（面试加分点）

- 订单按时间或用户维度分库分表（中后期）
- 建联合索引：`(user_id, created_at)`、`(order_no)`、`(status, created_at)`
- 避免大事务；批处理使用分片批次提交
- 归档冷数据到历史表，降低主表膨胀

## 12. 分阶段落地

1. V1：单体模块（MySQL + Redis + MQ）先跑通交易闭环
2. V2：接入支付渠道、延迟关单、权益发放异步化
3. V3：引入 ES 搜索、分库分表、全链路观测与压测平台

## 13. 面试表达模板（可直接说）

- “我把视频商城拆成商品、订单、支付、权益四条核心链路，主交易使用本地事务，跨服务靠 Outbox + MQ 做最终一致性。”
- “高并发下单采用 Redis Lua 预扣库存 + MySQL 条件更新兜底，避免超卖，并用延迟消息实现自动关单回补。”
- “支付回调严格幂等，按业务单号和渠道流水去重，状态机转移带乐观锁，防止并发乱序。”
- “观测上关注下单成功率、库存一致率、死信堆积，出问题可快速定位在库存、支付还是发放链路。”

---

如果你后续要继续做实现版，我可以基于你当前仓库结构再补一份：

- `api`/`service`/`dao` 的包结构与类清单
- MySQL DDL（可直接执行）
- Redis key 设计与过期策略
- 核心接口的请求响应字段定义
