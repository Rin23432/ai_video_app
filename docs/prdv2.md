下面给你一份**“创作者社区”模块**的 **PRD + 技术方案（AI 可读版）**。它的写法刻意偏“工程交付”，方便你直接丢给 AI 生成代码：包含**范围、页面、状态机、接口、表结构、缓存、幂等、验收清单**。你可以把它作为 `PRD_CREATOR_COMMUNITY.md` 放进仓库当真理源 📌✨

---

# 创作者社区模块 PRD + 技术方案（AI 可读版）

## 0. 模块目标

在现有“AI 漫剧生成器”基础上新增 **创作者社区**：用户发布作品到社区，其他用户可浏览、点赞、评论、收藏、关注作者（关注为可选扩展）。模块参考 LOFTER 的“内容流 + 互动 + 个人主页/作品集”体验，但**本期不做商业化**。

## 1. 版本范围

### 1.1 MVP（本期必须交付）

- 作品发布（从“我的作品”/“作品详情”发布到社区）
- 社区内容流（发现页）：热门/最新 两个 tab（简单规则）
- 作品详情页：播放/图文展示 + 点赞 + 收藏 + 评论列表 + 发表评论
- 我的：我的发布作品管理（下架/删除）
- 基础内容安全：敏感词过滤（文本）+ 管理员下架

### 1.2 V1（下一期扩展）

- 关注/粉丝系统 + 关注流
- @提及、评论通知
- 标签系统（圈子/话题）+ 搜索增强（ES）
- 排行榜（周榜/月榜）

---

## 2. 角色与权限

- 游客：可浏览公开内容；不可点赞/评论/收藏/发布
- 普通用户：可发布、点赞、评论、收藏；可删除自己的评论/作品
- 管理员：可下架任何作品、删除违规评论、封禁用户（后台接口）

---

## 3. 核心对象定义（领域模型）

- **Content（社区作品）**：创作者发布的作品（视频漫剧或图文）
- **Comment（评论）**：作品下评论，支持一级评论 + 二级回复（MVP 可先只做一级）
- **Like（点赞）**：用户对作品点赞（可取消）
- **Favorite（收藏）**：用户收藏作品（可取消）
- **User（用户）**：作者、评论者、点赞者

---

## 4. 用户故事（User Stories）

1. 作为用户，我想在发现页刷到别人发布的漫剧，点开能看详情并互动。
2. 作为用户，我想给喜欢的作品点赞/收藏，之后在“我的收藏”里能找到。
3. 作为用户，我想评论作品并看到评论列表，作者也能看到并管理。
4. 作为作者，我想把我生成的作品发布到社区，并能下架/删除。
5. 作为管理员，我想对违规作品下架、对违规评论删除。

---

## 5. 交互与页面 PRD

## 5.1 页面结构（Android Compose）

新增导航入口：`CreatorCommunity`（发现页）

### A. 发现页（CommunityFeedScreen）

- Tabs：**最新** / **热门**
- 列表 Item 展示：
  - 封面图 coverUrl
  - 标题 title
  - 作者 avatar + nickname
  - 点赞数 likeCount、评论数 commentCount、收藏数 favoriteCount

- 点击 Item：进入作品详情 `ContentDetailScreen(contentId)`
- 下拉刷新 + 上拉分页

**最新**排序规则：按 `publishTime desc`
**热门**排序规则（MVP）：按 `hotScore desc`（后端计算或 Redis zset）

### B. 作品详情页（ContentDetailScreen）

- 内容展示（视频用 Media3 播放；图文则图片列表）
- 作者信息区域（头像、昵称、进入作者主页 - V1）
- 互动按钮：
  - 点赞（toggle）
  - 收藏（toggle）
  - 评论入口（拉起输入框）

- 评论列表：
  - 一级评论列表（MVP）
  - 每条评论：头像、昵称、内容、时间；如果是作者/本人可删除

### C. 发布页（PublishContentScreen）

入口：从“我的作品 WorkDetail”点击“发布到社区”

- 填写：标题（必填）、简介（可选）、标签（V1）、可见性（MVP 只公开）
- 发布按钮：调用后端发布接口，成功后跳转内容详情页

### D. 我的收藏页（MyFavoritesScreen）

- 展示收藏过的作品列表
- 可取消收藏

### E. 我的发布页（MyPublishedScreen）

- 展示我发布的作品
- 支持：下架、删除（删除=不可恢复；下架=不可见但保留数据）

---

## 6. 状态机与业务规则

## 6.1 Content 状态

- `DRAFT`：草稿（生成后但未发布）
- `PUBLISHED`：已发布（社区可见）
- `HIDDEN`：作者下架（仅作者可见）
- `REMOVED`：管理员下架/删除（不可见）

规则：

- `DRAFT -> PUBLISHED`：发布
- `PUBLISHED -> HIDDEN`：作者下架
- `HIDDEN -> PUBLISHED`：作者重新上架（可选）
- `PUBLISHED/HIDDEN -> REMOVED`：管理员操作
- `PUBLISHED -> REMOVED`：违规直接下架

## 6.2 Like/Favorite 规则

- 点赞/收藏均为 toggle：重复请求应幂等
- 每个用户对同一 content 只允许 0/1 条 like 或 favorite 记录
- 点赞/收藏计数：最终一致（可缓存 + 异步落库）

## 6.3 Comment 规则

- 评论内容长度限制：1 ~ 300 字（MVP）
- 敏感词过滤：命中则拒绝（返回错误码）
- 删除权限：本人或管理员可删除；作者可删除作品下所有评论（可选）

---

## 7. 后端技术方案（SSM + MyBatis + Redis + MySQL）

## 7.1 后端模块拆分（建议）

- `community-controller`（或在已有 api 模块内新增 package）
- `community-service`
- `community-dao`
- `community-common`（错误码、DTO）

---

## 8. MySQL 表设计（SQL 必须生成）

### 8.1 `content`（社区作品）

> 如果你已有 `work` 表（生成作品），建议做**分层**：

- `work` = 生成产物
- `content` = 社区发布实体（引用 workId）
  这样更像成熟架构（生成系统与社区系统解耦）。

字段：

- `id` BIGINT PK
- `work_id` BIGINT NOT NULL（引用生成作品）
- `author_id` BIGINT NOT NULL
- `title` VARCHAR(128) NOT NULL
- `description` VARCHAR(512) NULL
- `media_type` VARCHAR(16) NOT NULL（VIDEO/IMAGE）
- `cover_url` VARCHAR(512) NOT NULL
- `media_url` VARCHAR(512) NOT NULL（视频或首图/图文入口）
- `status` VARCHAR(16) NOT NULL（DRAFT/PUBLISHED/HIDDEN/REMOVED）
- `like_count` INT NOT NULL DEFAULT 0
- `favorite_count` INT NOT NULL DEFAULT 0
- `comment_count` INT NOT NULL DEFAULT 0
- `hot_score` BIGINT NOT NULL DEFAULT 0
- `publish_time` DATETIME NULL
- `created_at` DATETIME NOT NULL
- `updated_at` DATETIME NOT NULL

索引：

- `idx_content_publish_time(publish_time)`
- `idx_content_hot_score(hot_score)`
- `idx_content_author(author_id, publish_time)`
- `idx_content_status(status, publish_time)`

### 8.2 `content_like`

- `id` BIGINT PK
- `content_id` BIGINT NOT NULL
- `user_id` BIGINT NOT NULL
- `created_at` DATETIME NOT NULL
  唯一索引：
- `uk_like(content_id, user_id)`

### 8.3 `content_favorite`

- `id` BIGINT PK
- `content_id` BIGINT NOT NULL
- `user_id` BIGINT NOT NULL
- `created_at` DATETIME NOT NULL
  唯一索引：
- `uk_fav(content_id, user_id)`

### 8.4 `content_comment`

（MVP 一级评论）

- `id` BIGINT PK
- `content_id` BIGINT NOT NULL
- `user_id` BIGINT NOT NULL
- `text` VARCHAR(300) NOT NULL
- `status` VARCHAR(16) NOT NULL（NORMAL/DELETED）
- `created_at` DATETIME NOT NULL
  索引：
- `idx_comment_content(content_id, created_at)`
- `idx_comment_user(user_id, created_at)`

（V1 二级回复扩展字段）

- `parent_id` BIGINT NULL（回复哪条评论）

---

## 9. Redis 设计（缓存、榜单、限流）

### 9.1 热门榜（推荐）

- `zset:content:hot`
  member = contentId
  score = hotScore
  更新方式：
- 点赞/收藏/评论发生时增量更新 score（如 +2/+3/+5）
- 定时任务每小时把 zset topN 回写 `content.hot_score`（可选）

### 9.2 计数缓存（可选）

- `hash:content:count:{contentId}`
  fields: likeCount, favoriteCount, commentCount
  用于快速读，最终以 MySQL 为准（最终一致）

### 9.3 限流

- `rl:like:{userId}:{contentId}`（防连点，1s）
- `rl:comment:{userId}:{minute}`（每分钟最多 N 条）

---

## 10. API 设计（RESTful，必须实现）

### 10.1 发布内容

`POST /api/v1/community/contents`
Request：

```json
{
  "workId": 10001,
  "title": "第一集：樱花树下",
  "description": "一个雨夜的相遇"
}
```

Response：

```json
{ "contentId": 20001 }
```

规则：

- work 必须属于当前用户
- work 必须 READY（有 videoUrl/coverUrl）

### 10.2 内容流（分页）

`GET /api/v1/community/contents?tab=latest&cursor=0&limit=20`
`tab`：latest/hot

Response：

```json
{
  "items": [
    {
      "contentId": 20001,
      "title": "...",
      "coverUrl": "...",
      "author": { "userId": 1, "nickname": "...", "avatarUrl": "..." },
      "likeCount": 12,
      "favoriteCount": 3,
      "commentCount": 4,
      "publishTime": "2026-02-15T12:00:00"
    }
  ],
  "nextCursor": 20
}
```

### 10.3 内容详情

`GET /api/v1/community/contents/{contentId}`
Response（包含当前用户互动状态）：

```json
{
  "contentId": 20001,
  "workId": 10001,
  "title": "...",
  "description": "...",
  "mediaType": "VIDEO",
  "coverUrl": "...",
  "mediaUrl": "...",
  "author": { "userId": 1, "nickname": "...", "avatarUrl": "..." },
  "likeCount": 12,
  "favoriteCount": 3,
  "commentCount": 4,
  "viewerState": { "liked": true, "favorited": false },
  "publishTime": "..."
}
```

### 10.4 点赞 Toggle（幂等）

`POST /api/v1/community/contents/{contentId}/like`
Request：

```json
{ "action": "TOGGLE" }
```

Response：

```json
{ "liked": true, "likeCount": 13 }
```

### 10.5 收藏 Toggle（幂等）

`POST /api/v1/community/contents/{contentId}/favorite`
Response：

```json
{ "favorited": true, "favoriteCount": 4 }
```

### 10.6 评论列表

`GET /api/v1/community/contents/{contentId}/comments?cursor=0&limit=20`
Response：

```json
{
  "items": [
    { "commentId": 9001, "user": {...}, "text": "...", "createdAt": "..." }
  ],
  "nextCursor": 20
}
```

### 10.7 发表评论

`POST /api/v1/community/contents/{contentId}/comments`
Request：

```json
{ "text": "太好看了！" }
```

Response：

```json
{ "commentId": 9001, "commentCount": 5 }
```

### 10.8 删除评论

`DELETE /api/v1/community/comments/{commentId}`

### 10.9 我的收藏

`GET /api/v1/community/me/favorites?cursor=0&limit=20`

### 10.10 我的发布

`GET /api/v1/community/me/contents?cursor=0&limit=20`

### 10.11 下架/删除（作者）

`POST /api/v1/community/contents/{contentId}/hide`
`DELETE /api/v1/community/contents/{contentId}`

### 10.12 管理员下架（可选）

`POST /api/v1/admin/community/contents/{contentId}/remove`

---

## 11. 错误码规范（示例）

- `40001` 参数非法
- `40100` 未登录
- `40300` 无权限（不是作者/管理员）
- `40400` content 不存在
- `40901` 重复发布（同 workId 已发布）
- `42900` 操作太频繁（限流）
- `45100` 命中敏感词（文本违规）

---

## 12. 幂等与一致性策略（必须实现）

### 12.1 点赞/收藏幂等

实现方式：

- DB 唯一索引 `uk_like(content_id, user_id)`
- Toggle 逻辑：
  - 尝试 insert：成功则 liked=true 并 count+1
  - insert 失败（duplicate）则 delete 并 count-1

- count 更新必须在事务内，且不得小于 0

### 12.2 评论计数一致性

- 写评论成功：`content.comment_count + 1`
- 删除评论：`comment.status = DELETED`，`content.comment_count - 1`（>=0）
- 列表查询只读 NORMAL

---

## 13. Android 客户端技术落地（Compose 任务清单）

### 13.1 新增路由

- `community/feed`
- `community/detail/{contentId}`
- `community/publish/{workId}`
- `community/myFavorites`
- `community/myPublished`

### 13.2 ViewModel 状态（必须）

- FeedState：loading、items、nextCursor、error
- DetailState：content、comments、viewerState、submittingComment、error
- PublishState：title、desc、posting、error

### 13.3 UI/交互要求

- 点赞/收藏：乐观更新（先改 UI）+ 失败回滚（toast）
- 评论：发送后插入列表头部 + 清空输入框
- 退出详情页停止任何轮询/请求（协程 cancel）

---

## 14. 验收清单（AI 必须提供）

AI 生成代码后必须给出以下可验证内容：

1. `schema.sql`：包含新增 4 张表（content/content_like/content_favorite/content_comment）
2. `api.http`：至少 20 条测试用例，覆盖：
   - 发布、内容流、详情
   - 点赞/取消点赞
   - 收藏/取消收藏
   - 评论新增/列表/删除
   - 权限校验（游客点赞应失败）
   - 限流（连续点赞触发 429）

3. `README`：一键启动步骤（docker compose + 后端启动 + worker + app baseUrl 配置）
4. Android App 可演示闭环：
   - 从 WorkDetail 发布到社区
   - 发现页看到内容
   - 详情页点赞/收藏/评论成功
   - 我的收藏能看到
   - 我的发布能下架/删除

---
