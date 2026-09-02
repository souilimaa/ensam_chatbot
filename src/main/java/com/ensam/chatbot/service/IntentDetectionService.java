package com.ensam.chatbot.service;

import com.ensam.chatbot.intent.Intent;
import com.ensam.chatbot.intent.IntentType;
import com.ensam.chatbot.llm.OllamaClient;
import com.ensam.chatbot.llm.PromptFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IntentDetectionService {

    private static final Logger log = LoggerFactory.getLogger(IntentDetectionService.class);

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public IntentDetectionService(OllamaClient ollamaClient, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public Intent detect(String question) {
        try {
            String response = ollamaClient.generate(PromptFactory.intentPrompt(question));
            return objectMapper.readValue(extractJsonObject(response), Intent.class);
        } catch (Exception exception) {
            log.warn("Unable to classify chatbot question: {}", exception.getMessage());
            Intent fallback = new Intent();
            fallback.setIntent(IntentType.UNKNOWN);
            return fallback;
        }
    }

    private String extractJsonObject(String response) {
        if (response == null) {
            return "{}";
        }
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return "{}";
        }
        return response.substring(start, end + 1).trim();
    }
}
