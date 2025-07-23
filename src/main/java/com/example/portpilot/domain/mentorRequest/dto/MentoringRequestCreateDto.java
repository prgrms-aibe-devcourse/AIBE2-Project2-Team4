package com.example.portpilot.domain.mentorRequest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentoringRequestCreateDto {
    private String mentorEmail;
    private String topic;
    private String message;
}

