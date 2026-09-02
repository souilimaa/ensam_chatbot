package com.ensam.chatbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ollama")
public record OllamaProperties(
        String baseUrl,
        String model,
        Duration connectTimeout,
        Duration responseTimeout
) {
}
