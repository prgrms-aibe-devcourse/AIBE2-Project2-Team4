package com.example.portpilot.domain.study.dto;

import com.example.portpilot.domain.study.entity.JobType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class StudyDetailResponseDto {

    private Long id;
    private String title;
    private String content;
    private String techStack;

    private int maxMembers;
    private int backendRecruit;
    private int frontendRecruit;
    private int designerRecruit;
    private int plannerRecruit;

    private LocalDateTime deadline;
    private boolean isClosed;
    private boolean isBlocked;

    private List<StudyParticipantDto> participants;
}
