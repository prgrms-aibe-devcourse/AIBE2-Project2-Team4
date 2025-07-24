package com.example.portpilot.domain.profile.dto;

import com.example.portpilot.domain.project.entity.ParticipationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParticipationDto {
    /** Participation 엔티티의 ID */
    private Long id;

    /** 요청한 프로젝트 ID */
    private Long projectId;

    /** 요청한 프로젝트 제목 */
    private String projectTitle;

    /** 요청 일시 */
    private LocalDateTime requestedAt;

    /** 요청 상태 (PENDING, APPROVED, REJECTED) */
    private ParticipationStatus status;
}
