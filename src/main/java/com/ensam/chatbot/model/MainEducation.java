package com.ensam.chatbot.model;

import lombok.Data;

@Data
public class MainEducation {
    private String school;
    private String degree;
    private String majorRaw;
    private String majorNorm;
    private Integer startYear;
    private Integer endYear;
    private Boolean schoolIsENSAM;
}
