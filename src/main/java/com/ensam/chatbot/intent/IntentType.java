package com.ensam.chatbot.intent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum IntentType {
    PERSON_PFE,
    PERSON_MAJOR,
    PERSON_SKILLS,
    PERSON_CURRENT_JOB,
    ANALYTICS_TOP,

    @JsonEnumDefaultValue
    UNKNOWN;

    @JsonCreator
    public static IntentType from(String value) {
        if (value == null) return UNKNOWN;
        String v = value.trim().toUpperCase();
        try {
            return IntentType.valueOf(v);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
