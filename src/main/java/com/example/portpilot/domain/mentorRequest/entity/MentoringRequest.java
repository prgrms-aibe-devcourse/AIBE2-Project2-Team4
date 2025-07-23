package com.example.portpilot.domain.mentorRequest.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentoring_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신청자 (멘티)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //멘티 이름
    @Column(name = "user_name")
    private String userName;

    // 멘토
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private User mentor;

    //멘토 이름
    @Column(name = "mentor_name")
    private String mentorName;

    //멘토링이 끝났는지
    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private MentoringStatus status;

    // 멘토링 날
    private LocalDateTime scheduledAt;

    private String SessionUrl;

    @PrePersist
    public void init() {
        if (status == null) {
            status = MentoringStatus.PENDING;
        }
    }
}
