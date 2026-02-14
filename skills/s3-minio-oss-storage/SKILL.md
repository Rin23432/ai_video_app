---
name: s3-minio-oss-storage
description: Use this skill for S3-compatible object storage integration (MinIO or OSS) for generated media assets.
---

# Object Storage (S3/MinIO/OSS) Skill

## Goal

把 AI 生成资产（视频/封面/分镜 JSON 等）落到对象存储，返回稳定可访问 URL，并支持：

- bucket/key 命名规范
- public/private 访问策略
- 预签名 URL（private 场景）
- 生命周期管理（TTL/归档/清理）

## Use when

- 从 Mock URL 切换到真实文件产出
- worker 输出是本地文件，需要上传
- 需要支持 MinIO（本地）与 OSS/S3（线上）

## Inputs

- 资产类型：video/cover/script/frames
- workId/taskId/userId
- contentType、文件大小、校验（可选）

## Outputs

- `StorageClient`（upload/getUrl/presign）
- key 规范 + URL 生成策略
- 资产元数据写回 DB（videoUrl/coverUrl）

## Conventions

### Object key naming

建议：

- `animegen/{env}/{yyyy}/{MM}/{dd}/{workId}/{type}.{ext}`
  例：
- `animegen/dev/2026/02/14/ab12.../video.mp4`

### Access

- MVP：public read（简单）
- 进阶：private + presigned URL（更安全）

### Provider abstraction

- service/worker 只依赖 `StorageClient` 接口
- MinIO/S3/OSS 各自实现适配器

## Workflow

1. worker 产出文件（或拿到 provider 返回的 bytes/url）
2. 上传到对象存储（upload）
3. 得到可访问 URL（public 或 presigned）
4. 写回 DB：work.videoUrl/coverUrl
5. （可选）清理本地临时文件

## Checklist

- [ ] key 命名稳定且不冲突
- [ ] URL 可读可访问（最少做 HEAD/GET 验证）
- [ ] contentType 正确（video/mp4、image/jpeg）
- [ ] 上传失败可重试（幂等 key）
