package com.example.portpilot.domain.mentorReview.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentoringReviewCreateDto {
    private Long mentoringRequestId;  // 멘토링 후기 ID
    private String title;             // 후기 제목
    private String content;           // 후기 내용
    private Integer rating;           // 별점 (1-5)
}