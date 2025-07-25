package com.example.portpilot.domain.study.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "study_participation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyParticipation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 참여자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 참여한 스터디
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private StudyRecruitment study;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    private StudyApplyStatus status;

    @PrePersist
    public void onCreate() {
        this.status = StudyApplyStatus.PENDING;
    }
}
