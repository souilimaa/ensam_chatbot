package com.ensam.chatbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("profiles")
public class Profile {
    @Id
    private String id;

    private String fullName;
    private String headline;
    private String country;
    private String location;

    private MainEducation mainEducation;
    private List<Experience> experiences;
    private List<String> skills;
    private Pfe pfe;
}
