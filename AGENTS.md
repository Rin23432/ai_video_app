# AGENTS Instructions for `E:\projrcts\ai_web`

## Skills
A skill is a local instruction set stored in a `SKILL.md` file.

### Available skills
- android-kotlin-compose: Android client implementation with Kotlin + Jetpack Compose for page state, interaction flow, and API integration. (file: skills/android-kotlin-compose/SKILL.md)
- java-ssm-backend: Java backend implementation using Spring + Spring MVC + MyBatis for API, service, DAO, and module layering. (file: skills/java-ssm-backend/SKILL.md)
- redis-task-queue-cache: Redis usage for task queue, task status cache, and rate limiting in this project. (file: skills/redis-task-queue-cache/SKILL.md)
- mysql-schema-ddl: MySQL schema and DDL conventions for `user/work/task/asset` and index strategy from PRD. (file: skills/mysql-schema-ddl/SKILL.md)
- s3-minio-oss-storage: S3-compatible object storage integration (MinIO/OSS) for cover/video/audio/script assets. (file: skills/s3-minio-oss-storage/SKILL.md)
- ai-inference-adapters: AI inference adapter design for cloud model providers and optional on-device ONNX/TFLite path. (file: skills/ai-inference-adapters/SKILL.md)
- docker-nginx-deploy: Containerized deployment with Docker Compose + Nginx + MySQL + Redis + MinIO. (file: skills/docker-nginx-deploy/SKILL.md)

## How to use skills
- Trigger rules:
  - If user explicitly names a skill (with `$skill-name` or plain text), use that skill in the current turn.
  - If the task clearly matches one skill description above, use that skill automatically.
  - Use multiple skills only when necessary; prefer the minimal set that covers the task.
- Loading strategy:
  - Read only the required skill `SKILL.md` first.
  - Load additional files only when the skill explicitly requires them.
- Fallback:
  - If a skill file is missing or blocked, state it briefly and continue with the best direct implementation.

## Source of truth
- PRD technical stack source: `docs/prd.md`
