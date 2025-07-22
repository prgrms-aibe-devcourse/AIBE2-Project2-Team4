package com.example.portpilot.domain.resume.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ExperienceRequest {
    private String activityName;
    private String institution;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent = false;
    private String content;
}
