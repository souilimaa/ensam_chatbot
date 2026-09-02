package com.ensam.chatbot.controller;

import com.ensam.chatbot.dto.ChatResponse;
import com.ensam.chatbot.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void returnsChatResponseForValidQuestion() throws Exception {
        when(chatService.ask("Top skills for promo 2025"))
                .thenReturn(new ChatResponse("Java, Spring", "mongodb"));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Top skills for promo 2025\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Java, Spring"))
                .andExpect(jsonPath("$.source").value("mongodb"));

        verify(chatService).ask("Top skills for promo 2025");
    }

    @Test
    void rejectsBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Question is required"));
    }
}
