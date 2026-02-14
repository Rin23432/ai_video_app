---
name: redis-task-queue-cache
description: Use this skill for Redis queue, task status cache, and rate limit implementation in AnimeGen.
---

# Redis Task Queue & Cache Skill

## Goal

用 Redis 支持：

1. 任务队列：`queue:tasks`（List）
2. 状态缓存：`task:status:{taskId}`（String + TTL 7 days）

并保持一致性：DB 是最终真相，Redis 是加速层。

## Use when

- 需要改队列模型（List -> Stream）
- 需要增加重试、死信队列、延迟队列
- 需要优化任务状态查询性能
- 需要保证 worker 宕机/重启的可恢复性

## Inputs

- taskId/workId/prompt/userId
- 状态机：PENDING/RUNNING/SUCCEEDED/FAILED
- TTL：7 天（可配置）

## Outputs

- 队列 push/pop 逻辑
- 状态缓存 set/get 逻辑
- 可扩展：retry count、DLQ key、lock key

## Conventions

- Key 命名固定：
  - 队列：`queue:tasks`
  - 状态：`task:status:{taskId}`
- 状态写入策略：先落库，再写缓存（或同时写，失败回补）
- 读取策略：先 Redis，miss 回源 DB，并回填 Redis
- worker 消费建议：生产用阻塞 pop（BLPOP）或 Stream consumer group
- 不要把巨大 payload 塞进 Redis（prompt 过大建议只放 workId/taskId）

## Workflow

1. 创建任务：DB insert task + work；Redis set status PENDING；push queue
2. worker 消费：pop；写 RUNNING；执行；写 SUCCEEDED/FAILED
3. API 查询：先 Redis；miss 回源 DB；回填 Redis

## Failure & Recovery

- push 成功但 DB 失败：禁止（应先 DB 后 push）
- DB 成功但 push 失败：task 标记 FAILED（或 PENDING 并定时补偿）
- worker 取出后崩溃：List 模式无法 ack，可能丢任务；生产建议 Stream 或 RPOPLPUSH + processing list

## Checklist

- [ ] key 命名统一
- [ ] TTL 可配置且默认 7 days
- [ ] miss 回源并回填
- [ ] worker 写状态时同时更新 DB + cache
