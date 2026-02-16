package com.animegen.ai;

public interface AiProvider {
    VideoResult generateVideo(VideoRequest request);

    class VideoRequest {
        private String prompt;
        private String modelId;
        private String apiKey;
        private String styleId;
        private String aspectRatio;
        private Integer durationSec;

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getStyleId() { return styleId; }
        public void setStyleId(String styleId) { this.styleId = styleId; }
        public String getAspectRatio() { return aspectRatio; }
        public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }
        public Integer getDurationSec() { return durationSec; }
        public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    }

    class VideoResult {
        private String coverUrl;
        private String videoUrl;

        public VideoResult() {
        }

        public VideoResult(String coverUrl, String videoUrl) {
            this.coverUrl = coverUrl;
            this.videoUrl = videoUrl;
        }

        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    }
}
