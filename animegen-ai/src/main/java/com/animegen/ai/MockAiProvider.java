package com.animegen.ai;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "animegen.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {
    @Override
    public VideoResult generateVideo(VideoRequest request) {
        String cover = "https://example.com/mock/cover-" + System.currentTimeMillis() + ".jpg";
        String video = "https://example.com/mock/video-" + System.currentTimeMillis() + ".mp4";
        return new VideoResult(cover, video);
    }
}
