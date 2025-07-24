package com.example.portpilot.domain.project.entity;

import com.example.portpilot.domain.project.entity.ParticipationStatus;
import com.example.portpilot.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation")
@Getter
@Setter
@NoArgsConstructor
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신청한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 대상 프로젝트 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** 요청 상태 (PENDING, APPROVED, REJECTED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatus status;

    /** 요청 시각 */
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();
}