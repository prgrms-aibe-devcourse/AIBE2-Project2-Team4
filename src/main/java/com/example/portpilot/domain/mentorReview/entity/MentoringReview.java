package com.example.portpilot.domain.mentorReview.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "mentoring_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 멘티 (글 작성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    // 멘토
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    // 완료된 멘토링 참조
    @Column(name = "mentoring_request_id", nullable = false)
    private Long mentoringRequestId;

    // 후기 제목
    @Column(nullable = false)
    private String title;

    // 후기 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 별점 (1-5)
    @Column(nullable = false)
    private Integer rating;

    // 조회수
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    private boolean is_blocked;

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }
}