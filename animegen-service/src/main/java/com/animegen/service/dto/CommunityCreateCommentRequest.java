package com.animegen.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommunityCreateCommentRequest {
    @NotBlank
    @Size(min = 1, max = 300)
    private String text;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
