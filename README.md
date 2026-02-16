# AnimeGen APP

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)](https://redis.io/)
[![Android](https://img.shields.io/badge/Android-Compose-3DDC84?logo=android)](https://developer.android.com/jetpack/compose)

AnimeGen 是一个「AI 漫剧生成 + 创作者社区」项目，当前仓库已形成可运行闭环：

`游客登录 -> 创建作品 -> 异步任务生成 -> 查询任务 -> 查看作品 -> 发布社区 -> 最新/热门/同好浏览 -> 点赞/收藏/评论 -> 我的发布/收藏管理`

## 技术亮点

- 多模块后端分层清晰：`api/service/dao/common/ai/worker` 解耦，便于扩展与联调。
- 任务异步化链路完整：API 写入 Redis 队列，Worker 消费执行，支持超时控制（60s）与失败重试（最多 3 次）。
- AI 适配层可切换：`MockAiProvider`（默认）+ `HttpAiProvider`（可接入外部推理网关），支持请求级 `apiKey` 覆盖服务端默认 key。
- 社区热度机制工程化：Redis ZSet 维护内容/标签热度，数据库兜底回退，保证 Redis 冷启动也可读。
- 鉴权模型兼顾冷启动与互动安全：支持 guest token，互动接口对 guest 返回 `40100`，登录后同接口无缝可用。
- Android 端采用 Compose 单 Activity 架构，DataStore 持久化配置，Media3 播放视频，已预留端侧推理能力（TFLite + NDK/C++ 代码基础）。
- 可观测性与稳定性基础具备：全局异常处理、统一错误码、JSR-303 参数校验、`traceId` 日志贯穿。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.3.5
- Spring MVC
- MyBatis (`mybatis-spring-boot-starter 3.0.3`)
- MySQL 8
- Redis 7
- Maven 多模块

### Android

- Kotlin + Jetpack Compose
- Retrofit + OkHttp
- DataStore
- Media3 (ExoPlayer)
- TensorFlow Lite（端侧能力预留）

### 基础设施

- Docker Compose
- MinIO（S3 兼容对象存储，可选）

## 项目结构

```text
ai_web/
├── animegen-api/          # REST API 层（controller / exception handler）
├── animegen-service/      # 业务编排层（作品、社区、鉴权等）
├── animegen-dao/          # MyBatis Mapper + DO
├── animegen-common/       # 响应模型、错误码、鉴权上下文、JWT
├── animegen-ai/           # AI Provider 抽象与实现（mock/http）
├── animegen-worker/       # Redis 队列消费、任务执行与状态回写
├── animegen-android/      # Android 客户端（Compose）
├── sql/schema.sql         # 数据库初始化脚本
├── sql/mock-data.sql      # 联调用最小数据
├── docker-compose.yml     # MySQL/Redis/MinIO
├── api.http               # 接口调试脚本
└── .docs/                 # PRD/技术方案文档
```

## 当前已实现接口（按模块）

### 鉴权与用户

- `POST /api/v1/auth/guest`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/me`
- `PUT /api/v1/me/profile`

### 作品与任务

- `POST /api/v1/works`
- `GET /api/v1/works`
- `GET /api/v1/works/{workId}`
- `DELETE /api/v1/works/{workId}`
- `GET /api/v1/tasks/{taskId}`

### 社区

- `POST /api/v1/community/contents`
- `GET /api/v1/community/contents?tab=latest|hot`
- `GET /api/v1/community/contents/search?keyword=...`
- `GET /api/v1/community/contents/{contentId}`
- `POST /api/v1/community/contents/{contentId}/like`
- `POST /api/v1/community/contents/{contentId}/favorite`
- `GET /api/v1/community/contents/{contentId}/comments`
- `POST /api/v1/community/contents/{contentId}/comments`
- `DELETE /api/v1/community/comments/{commentId}`
- `GET /api/v1/community/me/favorites`
- `GET /api/v1/community/me/contents`
- `POST /api/v1/community/contents/{contentId}/hide`
- `DELETE /api/v1/community/contents/{contentId}`

### 标签

- `GET /api/v1/community/tags/hot`
- `GET /api/v1/community/tags/search?keyword=...`
- `GET /api/v1/community/tags/{tagId}`
- `GET /api/v1/community/tags/{tagId}/contents?tab=latest|hot`

说明：

- `animegen-service` 中已存在排行榜相关服务与调度代码（outbox/snapshot），但当前 `animegen-api` 尚未暴露 `community/rankings/*` 控制器。

## 核心流程

1. 客户端调用 `POST /api/v1/works` 创建作品。
2. 服务端写入 `work/task`，并把任务 payload 推入 Redis `queue:tasks`。
3. Worker 消费任务，调用 `AiProvider` 生成结果。
4. Worker 回写任务与作品状态：`PENDING -> RUNNING -> SUCCESS/FAIL`。
5. 客户端轮询 `GET /api/v1/tasks/{taskId}`，成功后读取作品详情中的 `videoUrl/coverUrl`。

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- Docker / Docker Compose
- 可选：Android Studio

### 2. 启动基础依赖

```bash
docker compose up -d
```

默认端口（宿主机）：

- MySQL: `localhost:3307`（容器内 `3306`）
- Redis: `localhost:6379`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

### 3. 配置应用

按需修改：

- `animegen-api/src/main/resources/application.properties`
- `animegen-worker/src/main/resources/application.properties`

关键配置：

- `spring.datasource.*`
- `spring.data.redis.*`
- `animegen.jwt.secret`
- `animegen.ai.provider`（worker：`mock` 或 `http`）

### 4. 构建

```bash
mvn -DskipTests clean package
```

### 5. 启动 API 与 Worker

```bash
mvn -pl animegen-api spring-boot:run
mvn -pl animegen-worker spring-boot:run
```

Windows 一键启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\start-backend.ps1
```

跳过构建：

```powershell
powershell -ExecutionPolicy Bypass -File .\start-backend.ps1 -SkipBuild
```

### 6. 切换真实 AI 推理（可选）

默认使用 `MockAiProvider`。如需接入 HTTP 推理网关，配置 worker：

```bash
ANIMEGEN_AI_PROVIDER=http
ANIMEGEN_AI_HTTP_URL=https://your-ai-gateway.example.com/generate
ANIMEGEN_AI_HTTP_API_KEY=your-api-key
ANIMEGEN_AI_HTTP_DEFAULT_MODEL_ID=qwen-vl-max-latest
ANIMEGEN_AI_HTTP_API_KEY_HEADER=Authorization
ANIMEGEN_AI_HTTP_API_KEY_PREFIX="Bearer "
```

请求体字段约定：

- `prompt`
- `modelId` / `model`
- `styleId`
- `aspectRatio`
- `durationSec`

响应需至少包含 `videoUrl`（可选 `coverUrl`）：

- `{ "videoUrl": "...", "coverUrl": "..." }`
- `{ "data": { "videoUrl": "...", "coverUrl": "..." } }`

## Android 客户端

目录：`animegen-android`

联调建议地址：

- Android 模拟器：`http://10.0.2.2:8080`
- 真机：`http://<你的局域网IP>:8080`

更多说明见：`animegen-android/README.md`

## 数据库

核心表：

- `user`
- `work`
- `task`
- `asset`
- `content`
- `content_like`
- `content_favorite`
- `content_comment`
- `tag`
- `content_tag`

完整 DDL：`sql/schema.sql`

## 文档

- PRD：`.docs/prd.md`
- 后端方案：`.docs/后端技术栈与排行榜改造方案.md`
- 端侧模型说明：`.docs/端侧模型接入说明-llama.cpp-3B-Q4-GGUF.md`

## License

当前仓库未提供 `LICENSE` 文件。
