package com.animegen.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "animegen.ai.provider", havingValue = "http")
public class HttpAiProvider implements AiProvider {
    private final AiProviderProperties properties;
    private final ObjectMapper objectMapper;

    public HttpAiProvider(AiProviderProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public VideoResult generateVideo(VideoRequest request) {
        AiProviderProperties.Http config = properties.getHttp();
        if (isBlank(config.getUrl())) {
            throw new IllegalStateException("animegen.ai.http.url is required when animegen.ai.provider=http");
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                    .build();

            String effectiveModelId = isBlank(request.getModelId()) ? config.getDefaultModelId() : request.getModelId();
            String effectiveApiKey = isBlank(request.getApiKey()) ? config.getApiKey() : request.getApiKey();
            if (isBlank(effectiveApiKey)) {
                throw new IllegalStateException("api key is required: request.apiKey or animegen.ai.http.api-key");
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("prompt", request.getPrompt());
            body.put("modelId", effectiveModelId);
            body.put("model", effectiveModelId);
            body.put("styleId", request.getStyleId());
            body.put("aspectRatio", request.getAspectRatio());
            body.put("durationSec", request.getDurationSec());

            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(config.getUrl()))
                    .header("Content-Type", "application/json")
                    .header(config.getApiKeyHeader(), config.getApiKeyPrefix() + effectiveApiKey)
                    .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("ai http call failed, status=" + response.statusCode() + ", body=" + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode payload = root.path("data").isObject() ? root.path("data") : root;
            String coverUrl = firstNonBlank(payload, "coverUrl", "cover_url");
            String videoUrl = firstNonBlank(payload, "videoUrl", "video_url");

            if (isBlank(videoUrl)) {
                throw new RuntimeException("ai http response missing videoUrl/video_url");
            }
            return new VideoResult(coverUrl, videoUrl);
        } catch (Exception ex) {
            throw new RuntimeException("ai http provider failed: " + ex.getMessage(), ex);
        }
    }

    private String firstNonBlank(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode v = node.get(key);
            if (v != null && !v.isNull() && !isBlank(v.asText())) {
                return v.asText();
            }
        }
        return null;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
