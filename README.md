# AnimeGen

AnimeGen 是一个 AI 漫剧生成原型项目，当前实现了 M0 版本的核心闭环：
`游客登录 -> 创建作品 -> 异步生成任务 -> 查询任务 -> 查看作品结果`。

## 当前实现范围（M0）
- 游客鉴权：`POST /api/v1/auth/guest` 返回 JWT
- 创建作品：`POST /api/v1/works`
- 任务查询：`GET /api/v1/tasks/{taskId}`
- 作品列表/详情/删除：`GET /api/v1/works`、`GET /api/v1/works/{id}`、`DELETE /api/v1/works/{id}`
- 异步任务链路：API 写入 Redis 队列，Worker 消费并更新任务与作品状态
- AI 适配层：已接入 `MockAiProvider`（返回模拟 `coverUrl`/`videoUrl`）
- Android 客户端：支持创建、轮询、作品列表、详情播放、配置服务地址

## 技术栈

### 后端
- Java 17
- Spring Boot 3.3.5
- Spring MVC
- MyBatis (mybatis-spring-boot-starter 3.0.3)
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
- MinIO（S3 兼容对象存储，当前阶段为可选）

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
├── docker-compose.yml   # mysql/redis/minio
└── api.http             # 接口调试脚本
```

## 核心流程
1. 客户端调用 `POST /api/v1/works` 创建作品。
2. 服务端写入 `work` 和 `task`，并将任务 payload 推入 Redis `queue:tasks`。
3. Worker 通过 `BRPOP` 消费任务，调用 `AiProvider` 生成结果。
4. Worker 回写：
- `task` 状态：`PENDING -> RUNNING -> SUCCESS/FAIL`
- `work` 状态：`GENERATING -> READY/FAIL`
5. 客户端轮询 `GET /api/v1/tasks/{id}`，成功后查询作品详情拿到 `videoUrl/coverUrl`。

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.9+
- Docker / Docker Compose
- （可选）Android Studio Hedgehog+

### 2. 启动依赖服务
```bash
docker compose up -d
```

会启动：
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

### 4. 构建项目
```bash
mvn -DskipTests clean package
```

### 5. 启动 API 和 Worker
```bash
mvn -pl animegen-api spring-boot:run
mvn -pl animegen-worker spring-boot:run
```

## 接口联调
可直接使用根目录 `api.http`，或按以下顺序：
1. `POST /api/v1/auth/guest` 获取 token
2. `POST /api/v1/works` 创建作品（返回 `workId`、`taskId`）
3. `GET /api/v1/tasks/{taskId}` 轮询任务
4. `GET /api/v1/works/{workId}` 查看生成结果

## Android 客户端
目录：`animegen-android`

运行要点：
1. 用 Android Studio 打开 `animegen-android`
2. 配置后端地址
- 模拟器：`http://10.0.2.2:8080`
- 真机：`http://<你的局域网IP>:8080`
3. 在 App `Settings` 页面设置 `baseUrl` 后进行创建与轮询

## 数据表
当前核心表：
- `user`
- `work`
- `task`
- `asset`

完整 DDL：`sql/schema.sql`

## 后续扩展建议
- 将 `MockAiProvider` 替换为真实云端模型/本地推理实现
- 接入 MinIO 文件上传与资产落盘（当前为模拟 URL）
- 增加任务重试、限流、幂等与可观测性
- 补充单元测试与集成测试
