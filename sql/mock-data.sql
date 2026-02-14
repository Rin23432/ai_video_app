USE animegen;

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
