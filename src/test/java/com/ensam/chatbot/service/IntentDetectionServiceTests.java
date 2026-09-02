package com.ensam.chatbot.service;

import com.ensam.chatbot.intent.Intent;
import com.ensam.chatbot.intent.IntentType;
import com.ensam.chatbot.llm.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntentDetectionServiceTests {

    private final OllamaClient ollamaClient = mock(OllamaClient.class);
    private final IntentDetectionService service =
            new IntentDetectionService(ollamaClient, new ObjectMapper());

    @Test
    void parsesJsonWrappedInModelText() {
        when(ollamaClient.generate(org.mockito.ArgumentMatchers.anyString())).thenReturn(
                "result: {\"intent\":\"ANALYTICS_TOP\",\"promoYear\":2025," +
                        "\"metrics\":[\"SKILLS\"],\"topK\":5}");

        Intent intent = service.detect("Top five skills for promo 2025");

        assertThat(intent.getIntent()).isEqualTo(IntentType.ANALYTICS_TOP);
        assertThat(intent.getPromoYear()).isEqualTo(2025);
        assertThat(intent.getMetrics()).containsExactly("SKILLS");
        assertThat(intent.getTopK()).isEqualTo(5);
    }

    @Test
    void fallsBackToUnknownForInvalidModelOutput() {
        when(ollamaClient.generate(org.mockito.ArgumentMatchers.anyString())).thenReturn("not JSON");

        assertThat(service.detect("question").getIntent()).isEqualTo(IntentType.UNKNOWN);
    }
}
