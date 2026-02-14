---
name: android-kotlin-compose
description: Use this skill for Android app work in this repo using Kotlin and Jetpack Compose, including screens, state, API calls, and creation/task flows.
---

# Android Kotlin + Compose Skill (MVP)

## Goal

为 AnimeGen 客户端（安卓优先）提供清晰的 UI/数据流脚手架：

- 登录（guest token）
- 创建作品（prompt 输入）
- 任务轮询（taskId -> status）
- 作品详情（videoUrl/coverUrl 展示）

## Use when

- 准备做 Android MVP（哪怕先不做，skill 先占位）
- 需要定义 API client、数据模型、状态管理
- 需要二次元风格 UI（主题、字体、卡片、动效）

## Inputs

- API endpoints：
  - POST /api/v1/auth/guest
  - POST /api/v1/works
  - GET /api/v1/tasks/{taskId}
  - GET /api/v1/works/{workId}
- 返回结构：ApiResponse<T>

## Outputs

- Retrofit/Ktor client
- Repository + ViewModel（单向数据流）
- Compose screens：
  - Home/Create
  - TaskProgress
  - WorkDetail

## Conventions

- 网络层统一拦截器注入 Bearer token
- 轮询采用协程 + 取消机制（离开页面即停止）
- 缓存 token 到 DataStore（轻量）
- UI 状态：Loading/Success/Error 明确分支

## Checklist

- [ ] guest 登录拿 token
- [ ] 创建 work 返回 workId/taskId
- [ ] 轮询直到 SUCCEEDED/FAILED
- [ ] 展示 cover + video（先用占位/外链播放）
