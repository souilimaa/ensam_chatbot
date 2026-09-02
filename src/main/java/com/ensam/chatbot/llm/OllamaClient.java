package com.ensam.chatbot.llm;

import com.ensam.chatbot.config.OllamaProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OllamaClient {

    private final WebClient webClient;
    private final OllamaProperties properties;

    public OllamaClient(WebClient ollamaWebClient, OllamaProperties properties) {
        this.webClient = ollamaWebClient;
        this.properties = properties;
    }

    public String generate(String prompt) {
        OllamaResponse response = webClient.post()
                .uri("/api/generate")
                .bodyValue(new OllamaRequest(properties.model(), prompt, false))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.createException().map(OllamaException::new))
                .bodyToMono(OllamaResponse.class)
                .block(properties.responseTimeout());

        if (response == null || response.response() == null || response.response().isBlank()) {
            throw new OllamaException("Ollama returned an empty response");
        }
        return response.response();
    }

    record OllamaRequest(String model, String prompt, boolean stream) {
    }

    record OllamaResponse(String response) {
    }
}
