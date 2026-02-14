# AnimeGen Android (Kotlin + Compose)

基于当前后端 API（`/api/v1/auth/guest`, `/api/v1/works`, `/api/v1/tasks/{id}`, `/api/v1/works/{id}`）的 Android 客户端。

## 技术栈
- Kotlin + Jetpack Compose
- Retrofit + OkHttp
- Media3 (WorkDetail 视频播放)
- DataStore (token/baseUrl/deviceId 持久化)

## 页面
- `Create`: 发起 `POST /api/v1/works`
- `Task`: 每 1.5s 轮询 `GET /api/v1/tasks/{id}`，成功后跳 `WorkDetail`
- `Works`: 拉取作品列表，展示封面 + 标题 + 状态
- `WorkDetail`: 拉取作品详情并用 Media3 播放 `videoUrl`
- `Settings`: 配置 `baseUrl`、`deviceId`

## 全局错误处理
统一映射并在页面显式展示 + 重试按钮：
- 网络失败（`IOException`）
- `401`（token 失效）
- `5xx`（服务端异常）
- 业务错误（`ApiResponse.code != 0`）
- 任务失败（`status=FAIL`）

## 配置 baseUrl
有两种方式：

1. Gradle 默认值（编译时）
- 文件：`gradle.properties`
- 修改：

```properties
API_BASE_URL=mock://offline
```

`mock://offline` 为纯离线模式，不依赖后端服务。

2. App 内动态修改（运行时）
- 进入 `Settings` 页面修改 `baseUrl`
- 点击保存后会清空 token，后续请求自动重新获取 guest token

## 运行
1. 用 Android Studio 打开 `animegen-android`
2. 等待 Gradle Sync 完成
3. 选择设备运行 `app`

默认 `API_BASE_URL=mock://offline`，可离线跑通最小 Mock 流程（Create -> Task -> Works -> WorkDetail）。

建议联调地址：
- Android 模拟器访问宿主机后端：`http://10.0.2.2:8080`
- 真机访问局域网后端：`http://<你的电脑局域网IP>:8080`

## 联调步骤
1. 启动后端服务（确保 `api.http` 中接口可用）
2. 可选导入最小 mock 数据：执行 `sql/mock-data.sql`
3. 打开 App `Settings`，将 `baseUrl` 改为真实后端地址
4. 在 `Create` 输入标题和 prompt，点击 `POST /works`
5. 自动进入 `Task` 轮询，成功后自动跳 `WorkDetail`
6. 进入 `Works` 校验列表展示

说明：
- `Create` 请求会携带并复用 `requestId`，重试可命中后端幂等。
- `Task` 页面退出时会取消轮询，避免后台持续请求。

## 目录（关键）
- `app/src/main/java/com/animegen/app/MainActivity.kt`：导航与页面装配
- `app/src/main/java/com/animegen/app/data/network/NetworkClient.kt`：Retrofit/OkHttp 与错误映射
- `app/src/main/java/com/animegen/app/data/repo/*`：仓库与 401 重试
- `app/src/main/java/com/animegen/app/ui/screen/create/*`
- `app/src/main/java/com/animegen/app/ui/screen/task/*`
- `app/src/main/java/com/animegen/app/ui/screen/works/*`
- `app/src/main/java/com/animegen/app/ui/screen/workdetail/*`
- `app/src/main/java/com/animegen/app/ui/screen/settings/*`
- `app/src/main/java/com/animegen/app/ui/common/ErrorNotice.kt`：可见错误提示 + 重试按钮
