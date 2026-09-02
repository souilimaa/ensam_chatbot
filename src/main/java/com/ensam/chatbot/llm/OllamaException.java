package com.ensam.chatbot.llm;

public class OllamaException extends RuntimeException {
    public OllamaException(String message) {
        super(message);
    }

    public OllamaException(Throwable cause) {
        super("Ollama request failed", cause);
    }
}
