package com.animegen.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "animegen.ai")
public class AiProviderProperties {
    private String provider = "mock";
    private final Http http = new Http();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Http getHttp() {
        return http;
    }

    public static class Http {
        private String url;
        private String apiKey;
        private String defaultModelId;
        private String apiKeyHeader = "Authorization";
        private String apiKeyPrefix = "Bearer ";
        private Integer connectTimeoutMs = 10000;
        private Integer readTimeoutMs = 60000;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getDefaultModelId() {
            return defaultModelId;
        }

        public void setDefaultModelId(String defaultModelId) {
            this.defaultModelId = defaultModelId;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKeyPrefix() {
            return apiKeyPrefix;
        }

        public void setApiKeyPrefix(String apiKeyPrefix) {
            this.apiKeyPrefix = apiKeyPrefix;
        }

        public Integer getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(Integer connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public Integer getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(Integer readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}
