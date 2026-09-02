package com.ensam.chatbot.controller;

import com.ensam.chatbot.dto.ChatRequest;
import com.ensam.chatbot.dto.ChatResponse;
import com.ensam.chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        return chatService.ask(req.getQuestion());
    }
}
