---
name: docker-nginx-deploy
description: Use this skill for deployment tasks with Docker Compose, Nginx, MySQL, Redis, and MinIO.
---

# Docker / Nginx Deploy Skill

## Goal

提供本地与云上可复现的部署方式：

- infra: MySQL + Redis（docker compose）
- app: animegen-api + animegen-worker（容器化可选）
- 可选：Nginx 反代 API、静态资源、未来的前端

## Use when

- 需要一键启动开发环境
- 需要上云/上服务器部署
- 需要配置反向代理与 TLS（后续）

## Inputs

- 端口：API 8080，Worker 8081（或不暴露）
- 环境变量：DB/Redis/JWT secret
- 网络：同一 compose network

## Outputs

- docker-compose.yml（infra + app 可选）
- Dockerfile（api/worker）
- nginx.conf（反向代理，可选）

## Conventions

- 生产配置通过环境变量注入，不把 secret 写进 repo
- Nginx 只做反代与静态，不塞业务逻辑
- healthcheck（可选但推荐）

## Workflow (recommended)

1. 先 infra compose
2. API/Worker 本地运行验证
3. 再容器化 API/Worker（Dockerfile + compose）
4. 可选：nginx 统一入口（/api -> api container）

## Checklist

- [ ] `docker compose up -d` 可启动 mysql/redis
- [ ] API 能连 DB/Redis
- [ ] worker 能消费队列并回写结果
- [ ] 日志可从容器中查看
