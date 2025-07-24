package com.example.portpilot.domain.study.dto;

import com.example.portpilot.domain.study.entity.JobType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyParticipationRequestDto {
    private Long studyId;
    private JobType jobType;
}
