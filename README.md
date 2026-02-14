# AnimeGen

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)](https://redis.io/)
[![Android](https://img.shields.io/badge/Android-Compose-3DDC84?logo=android)](https://developer.android.com/jetpack/compose)

AnimeGen 是一个 AI 漫剧生成 + 创作者社区原型项目，当前实现了可运行闭环：
`游客登录 -> 创建作品 -> 异步生成任务 -> 查询任务 -> 查看作品结果 -> 发布到社区 -> 点赞/收藏/评论 -> 我的收藏/我的发布管理`。

## 目录
- [当前实现范围（M0）](#当前实现范围m0)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [核心流程](#核心流程)
- [快速开始](#快速开始)
- [接口联调](#接口联调)
- [Android 客户端](#android-客户端)
- [数据表](#数据表)
- [开发约束](#开发约束)
- [FAQ](#faq)
- [贡献指南](#贡献指南)
- [License](#license)

## 当前实现范围（M0 + 社区MVP）
- 游客鉴权：`POST /api/v1/auth/guest` 返回 JWT。
- 创建作品：`POST /api/v1/works`。
- 任务查询：`GET /api/v1/tasks/{taskId}`。
- 作品列表/详情/删除：`GET /api/v1/works`、`GET /api/v1/works/{id}`、`DELETE /api/v1/works/{id}`。
- 社区发布：`POST /api/v1/community/contents`（work ready 后发布）。
- 社区内容流：`GET /api/v1/community/contents?tab=latest|hot`。
- 社区详情：`GET /api/v1/community/contents/{contentId}`。
- 点赞/收藏 toggle：`POST /api/v1/community/contents/{contentId}/like|favorite`。
- 评论 CRUD：`GET/POST /api/v1/community/contents/{contentId}/comments`、`DELETE /api/v1/community/comments/{commentId}`。
- 我的收藏/我的发布：`GET /api/v1/community/me/favorites`、`GET /api/v1/community/me/contents`。
- 作者下架/删除：`POST /api/v1/community/contents/{contentId}/hide`、`DELETE /api/v1/community/contents/{contentId}`。
- 异步任务链路：API 写入 Redis 队列，Worker 消费并更新任务与作品状态。
- AI 适配层：已接入 `MockAiProvider`（返回模拟 `coverUrl` / `videoUrl`）。
- Android 客户端：支持社区 Feed/Detail/Publish/MyFavorites/MyPublished，详情页视频播放、点赞收藏乐观更新、评论发送刷新。
- 质量保障：全局异常拦截、错误码体系、JSR-303 参数校验、`traceId` 日志、事务写入、幂等创建、点赞收藏幂等（唯一索引 + 事务计数）、敏感词过滤、Redis 热榜回退策略。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.3.5
- Spring MVC
- MyBatis (`mybatis-spring-boot-starter 3.0.3`)
- MySQL 8
- Redis 7
- Maven 多模块

### 移动端
- Kotlin + Jetpack Compose
- Retrofit + OkHttp
- DataStore
- Media3 (ExoPlayer)

### 基础设施
- Docker Compose
- MinIO（S3 兼容对象存储，当前阶段可选）

## 项目结构
```text
ai_web/
├── animegen-api/        # REST API
├── animegen-service/    # 业务编排
├── animegen-dao/        # MyBatis Mapper / DO
├── animegen-common/     # 通用响应、异常、枚举、JWT
├── animegen-ai/         # AI Provider 抽象与 Mock 实现
├── animegen-worker/     # Redis 队列消费与任务执行
├── animegen-android/    # Android 客户端
├── sql/schema.sql       # 数据库初始化脚本
├── sql/mock-data.sql    # 最小可运行 mock 数据
├── docker-compose.yml   # mysql/redis/minio
└── api.http             # 接口调试脚本
```

## 核心流程
1. 客户端调用 `POST /api/v1/works` 创建作品。
2. 服务端写入 `work` 和 `task`，并将 payload 推入 Redis `queue:tasks`。
3. Worker 通过 `BRPOP` 消费任务，调用 `AiProvider` 生成结果。
4. Worker 回写状态：`task` 为 `PENDING -> RUNNING -> SUCCESS/FAIL`，`work` 为 `GENERATING -> READY/FAIL`。
5. 客户端轮询 `GET /api/v1/tasks/{id}`，成功后查询作品详情获取 `videoUrl` / `coverUrl`。

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.9+
- Docker / Docker Compose
- 可选：Android Studio Hedgehog+

### 2. 启动依赖服务
```bash
docker compose up -d
```

默认端口：
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

`sql/schema.sql` 会自动初始化表结构。

### 3. 配置应用
按需修改：
- `animegen-api/src/main/resources/application.properties`
- `animegen-worker/src/main/resources/application.properties`

重点配置：
- `spring.datasource.*`
- `spring.data.redis.*`
- `animegen.jwt.secret`（生产环境必须替换）
- `logging.pattern.level=%5p [traceId:%X{traceId:-}]`

### 4. 构建项目
```bash
mvn -DskipTests clean package
```

### 5. 启动 API 与 Worker
```bash
mvn -pl animegen-api spring-boot:run
mvn -pl animegen-worker spring-boot:run
```

## 接口联调
可直接使用根目录 `api.http`（已提供 20 条用例），或按以下顺序：
1. `POST /api/v1/auth/guest` 获取 token。
2. `POST /api/v1/works` 创建作品（建议传 `requestId`，返回 `workId`、`taskId`）。
3. `GET /api/v1/tasks/{taskId}` 轮询任务状态。
4. `GET /api/v1/works/{workId}` 查看生成结果。
5. `POST /api/v1/community/contents` 发布到社区。
6. 社区内容流/详情/点赞收藏/评论/我的列表接口联调。

导入最小 mock 数据：
```bash
mysql -uroot -proot animegen < sql/mock-data.sql
```

## Android 客户端
客户端目录：`animegen-android`

联调要点：
1. 用 Android Studio 打开 `animegen-android`。
2. 配置后端地址。
3. 在 App `Settings` 页面设置 `baseUrl` 后进行创建、发布、社区互动。

地址示例：
- 模拟器：`http://10.0.2.2:8080`
- 真机：`http://<你的局域网IP>:8080`

社区验收建议路径：
1. 在 `Create` 创建任务，等待 `Task` 成功。
2. 进入 `Works -> WorkDetail`，点击“发布到社区”。
3. 进入 `Community` Tab 查看 feed，打开详情。
4. 在详情页执行点赞/收藏（观察乐观更新）、发送评论并刷新列表。
5. 进入 `MyFavorites` 和 `MyPublished` 验证列表与下架/删除操作。

## 数据表
核心表：
- `user`
- `work`
- `task`
- `asset`
- `content`
- `content_like`
- `content_favorite`
- `content_comment`

完整 DDL：`sql/schema.sql`

## 开发约束
- `POST /api/v1/works` 幂等策略：优先使用 `requestId` 去重；未传 `requestId` 时，服务端对核心字段做哈希去重。
- Worker 超时控制：AI 执行超时默认 60 秒。
- Worker 重试策略：最大重试 3 次，超限后标记任务失败。

## FAQ
### 1. 为什么创建作品有时返回重复请求错误？
同一用户短时间内提交重复请求会被幂等保护拦截。建议每次创建传唯一 `requestId`。

### 2. 为什么任务一直是 `PENDING`？
通常是 Worker 未启动，或 Redis 连接异常。请确认 `animegen-worker` 正在运行且能访问 `6379`。

### 3. 为什么作品没有真实可播放视频？
当前 M0 使用 `MockAiProvider`，仅返回模拟 URL，不接入真实模型生成。

### 4. 如何关联后端日志排查问题？
请求可带 `X-Trace-Id`，服务端会在响应头透传并在日志打印同一 `traceId`。

### 5. Android 模拟器连不上本机服务怎么办？
请使用 `http://10.0.2.2:8080`，不要使用 `localhost`。

## 贡献指南
1. Fork 并创建分支：`feat/<name>` 或 `fix/<name>`。
2. 保持改动最小闭环，并附带测试步骤。
3. 提交 Pull Request，说明变更内容、影响范围和风险。
4. 合并前请至少通过一次本地构建：`mvn -DskipTests clean package`。

## License
当前仓库尚未声明开源许可证（未提供 `LICENSE` 文件）。
