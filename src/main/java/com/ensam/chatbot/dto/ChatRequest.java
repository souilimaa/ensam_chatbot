package com.ensam.chatbot.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ChatRequest {
    @NotBlank(message = "Question is required")
    @Size(max = 500, message = "Question must not exceed 500 characters")
    private String question;
}
