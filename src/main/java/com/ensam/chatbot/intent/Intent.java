package com.ensam.chatbot.intent;

import lombok.Data;

import java.util.List;

@Data
public class Intent {
    private IntentType intent;
    private String personName;
    private Integer promoYear;
    private String majorNorm;
    private List<String> metrics;
    private Integer topK;
}
