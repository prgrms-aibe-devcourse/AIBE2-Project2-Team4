package com.example.portpilot.domain.project.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter @Setter
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프로젝트 소유자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // 상태 (예: OPEN, CLOSED 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    // 제목과 설명
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 생성 일시
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 모집 마감일
    @Column(name = "deadline")
    private LocalDateTime deadline;

    // 예상 시작 옵션
    @Enumerated(EnumType.STRING)
    @Column(name = "start_option")
    private StartOption startOption;

    // 진행 분류
    @Enumerated(EnumType.STRING)
    @Column(name = "project_type")
    private ProjectType projectType;

    // 기획 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "planning_state")
    private PlanningState planningState;

    // 프로젝트 경험
    @Enumerated(EnumType.STRING)
    @Column(name = "experience")
    private Experience experience;

    // 협업 예정 인력
    @Enumerated(EnumType.STRING)
    @Column(name = "collaboration_option")
    private CollaborationOption collaborationOption;

    // 참여자 목록
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participation> participants = new HashSet<>();

    // 편의 메서드: 참여 추가/제거
    public void addParticipant(Participation participation) {
        participants.add(participation);
        participation.setProject(this);
    }

    public void removeParticipant(Participation participation) {
        participants.remove(participation);
        participation.setProject(null);
    }

    // 저장 직전에 createdAt 자동 세팅
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
