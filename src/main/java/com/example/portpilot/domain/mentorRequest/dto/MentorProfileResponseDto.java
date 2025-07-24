package com.example.portpilot.domain.mentorRequest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MentorProfileResponseDto {
    private String userName;
    private String email;
    private String techStack;
    private String description;
}
