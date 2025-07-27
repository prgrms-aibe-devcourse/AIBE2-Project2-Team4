package com.example.portpilot.domain.mentorReview.dto;

import com.example.portpilot.domain.mentorReview.entity.MentoringReview;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class MentoringReviewResponseDto {
    private Long id;
    private String reviewerName;      // 멘티명 (글쓴이)
    private String mentorName;        // 멘토명
    private String title;             // 제목
    private String content;           // 내용
    private Integer rating;           // 별점
    private Integer viewCount;        // 조회수
    private String createdAt;         // 작성일
    private boolean writer;         // 작성자 인지 판별


    public static MentoringReviewResponseDto fromEntity(MentoringReview review) {
        MentoringReviewResponseDto dto = new MentoringReviewResponseDto();
        dto.setId(review.getId());
        dto.setReviewerName(review.getReviewer().getName());
        dto.setMentorName(review.getMentor().getName());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setViewCount(review.getViewCount());
        dto.setCreatedAt(review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dto.setWriter(true);
        return dto;
    }
}