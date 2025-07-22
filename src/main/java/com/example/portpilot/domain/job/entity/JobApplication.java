package com.example.portpilot.domain.job.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_application")
@Getter
@Setter
@NoArgsConstructor
public class JobApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_position_id", nullable = false)
    private JobPosition jobPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "지원완료";

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    public void prePersist() {
        this.appliedAt = LocalDateTime.now();
    }
}