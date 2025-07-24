package com.example.portpilot.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 프로필 화면에서 보여줄 포트폴리오 정보 DTO
 */
@Getter
@AllArgsConstructor
public class PortfolioDto {
    /** Portfolio 엔티티의 ID */
    private Long id;

    /** 포트폴리오 제목 */
    private String title;

    /** 포트폴리오 설명 */
    private String description;

    /** 생성 일시 */
    private LocalDateTime createdAt;
}
