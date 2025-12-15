package com.ensam.chatbot.model;

import lombok.Data;

@Data
public class Experience {
    private String company;
    private String title;
    private String location;
    private String startDate; // "YYYY-MM-DD"
    private String endDate;   // null si current
    private Boolean jobStillWorking;
    private String type; // "JOB" / "INTERNSHIP"
    private String internshipType; // "PFE" / null
}
