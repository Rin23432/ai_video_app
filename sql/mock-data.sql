USE animegen;

INSERT INTO `user` (id, username, password_hash, phone, nickname, avatar_url, bio, role, device_id, created_at, updated_at)
VALUES
    (12345, 'demo_user', '8d969eef6ecad3c29a3a629280e686cff8fab8dfc52f7f6f3f0f6f0b0a6be9f7', NULL, 'Demo User', NULL, 'mock seed user', 'USER', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), updated_at = NOW();

INSERT INTO work (id, user_id, title, prompt, style_id, aspect_ratio, duration_sec, status, cover_url, video_url, created_at, updated_at)
VALUES
    (10001, 12345, 'Mock Ready Work', 'mock prompt ready', 'anime_shonen', '9:16', 30, 'READY',
     'https://example.com/mock/cover-ready.jpg', 'https://example.com/mock/video-ready.mp4', NOW(), NOW()),
    (10002, 12345, 'Mock Generating Work', 'mock prompt pending', 'anime_shonen', '9:16', 30, 'GENERATING',
     'https://example.com/mock/cover-pending.jpg', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO task (id, work_id, type, status, progress, stage, error_code, error_message, retry_count, trace_id, created_at, updated_at)
VALUES
    (20001, 10001, 'GENERATE_VIDEO', 'SUCCESS', 100, 'DONE', NULL, NULL, 0, 'mock-trace-success', NOW(), NOW()),
    (20002, 10002, 'GENERATE_VIDEO', 'RUNNING', 35, 'RENDER', NULL, NULL, 1, 'mock-trace-running', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO tag (id, name, description, content_count, hot_score, status, created_at, updated_at)
VALUES
    (1, '奇幻', '奇幻题材', 0, 50, 'ACTIVE', NOW(), NOW()),
    (2, '校园', '校园日常', 0, 40, 'ACTIVE', NOW(), NOW()),
    (3, '热血', '热血战斗', 0, 35, 'ACTIVE', NOW(), NOW()),
    (4, '搞笑', '轻松搞笑', 0, 25, 'ACTIVE', NOW(), NOW()),
    (5, '悬疑', '悬疑推理', 0, 20, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();
