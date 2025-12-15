package com.ensam.chatbot.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCount {

    @Field("_id")
    private String id;

    private Long count;
}
