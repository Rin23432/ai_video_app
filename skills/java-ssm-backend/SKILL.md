---
name: java-ssm-backend
description: Use this skill for Java backend tasks with Spring, Spring MVC, and MyBatis in this multi-module project.
---

# Java Backend (Spring Boot + Service/DAO) Skill

## Goal

以“分层架构 + 可演进”为目标维护后端：

- `animegen-api`: REST + 鉴权 + DTO + 错误处理
- `animegen-service`: 业务编排（创建 work/task、入队、状态更新）
- `animegen-dao`: 数据模型与持久化（JPA + Flyway）
- `animegen-common`: 通用模型、错误码、工具类
- `animegen-worker`: 异步任务执行（消费队列、调用 AI、回写结果）

## Use when

- 需要新增 API
- 需要改任务流（队列/状态机/重试）
- 需要新增数据表/字段
- 需要做错误码规范、统一异常处理、日志链路

## Inputs

- PRD/接口定义
- 现有实体：Work/Task
- 状态机：PENDING/RUNNING/SUCCEEDED/FAILED
- Redis Key 规范（queue:tasks、task:status:{taskId}）

## Outputs

- 新增/修改 Controller + Service + DAO（以及 Flyway SQL）
- 对应的 http 示例与文档更新

## Conventions

- Controller 只做参数校验、鉴权、DTO 映射；业务逻辑在 service
- service 事务边界清晰（必要时 `@Transactional`）
- DAO 层不写业务逻辑，只提供 Repository/Mapper
- Flyway 管理 schema，禁止在生产依赖 `ddl-auto=create`
- API 返回统一包装 `ApiResponse`
- 错误统一由 `@ControllerAdvice` 映射成 code/message

## Workflow

1. 写清楚 API contract（request/response、状态码、错误码）
2. 修改/新增 entity + Flyway migration
3. 实现 service（含事务、幂等、缓存一致性策略）
4. 实现 controller（校验、鉴权）
5. 写 api.http 用例 + 最小集成测试

## Checklist

- [ ] 新接口包含参数校验（@Validated + @NotBlank 等）
- [ ] 数据库变更通过 Flyway
- [ ] 缓存 key 与 TTL 符合约定
- [ ] 日志有 taskId/workId/userId
- [ ] worker 失败能落库 + 更新缓存

## Suggested Add-ons

- 全局异常：BusinessException(errorCode)
- MDC traceId：每个请求与 task 消费都带 traceId
- OpenAPI/Swagger（可选）
