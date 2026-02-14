---
name: mysql-schema-ddl
description: Use this skill for MySQL schema design, migrations, and query/index updates based on PRD entities.
---

# MySQL Schema (Flyway) Skill

## Goal

所有数据库结构变更通过 Flyway 管理，确保：

- 可回放（任何环境可从 0 build）
- 可审计（每次变更有版本）
- 可演进（字段新增、索引、约束可控）

## Use when

- 新增表/字段/索引
- 修改字段类型/长度
- 增加唯一约束/外键（可选）
- 需要性能优化（索引、分表前置准备）

## Inputs

- 业务实体：Work、Task
- 查询模式：按 workId 查 task、按 id 查 work
- URL 字段：videoUrl/coverUrl 可能很长（建议 1024）

## Outputs

- `animegen-dao/src/main/resources/db/migration/Vx__*.sql`
- entity 同步更新（JPA annotations）

## Conventions

- 禁止手动改生产表结构
- Flyway 文件命名：`V{number}__{desc}.sql`，number 递增
- 每次变更要考虑回滚策略（即使 Flyway 不自动回滚，也要可手工回滚）
- 必要索引必须加（例如 task(work_id)）
- 时间字段统一 UTC（TIMESTAMP）

## Checklist

- [ ] migration 可在空库执行成功
- [ ] migration 对已有数据兼容（默认值、nullable）
- [ ] 索引覆盖主要查询
- [ ] entity 字段类型与长度匹配
