package com.animegen.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateWorkRequest {
    @NotBlank
    @Size(max = 128)
    private String title;
    @NotBlank
    @Size(max = 2000)
    private String prompt;
    @NotBlank
    @Size(max = 64)
    private String styleId;
    @Size(max = 64)
    private String modelId;
    @Size(max = 256)
    private String apiKey;
    @NotBlank
    @Pattern(regexp = "^[0-9]+:[0-9]+$")
    private String aspectRatio;
    @Min(5)
    @Max(180)
    private Integer durationSec;
    @NotBlank
    @Size(max = 16)
    @Pattern(regexp = "^(CLOUD|DEVICE)$")
    private String mode;
    @Size(max = 64)
    private String requestId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getStyleId() { return styleId; }
    public void setStyleId(String styleId) { this.styleId = styleId; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
