package com.example.portpilot.domain.study.dto;

import com.example.portpilot.domain.study.entity.JobType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyParticipantDto {
    private Long userId;
    private String name;
    private String email;
    private JobType jobType;
}
