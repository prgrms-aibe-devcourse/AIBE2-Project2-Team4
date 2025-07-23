package com.example.portpilot.domain.project.entity;

import com.example.portpilot.domain.user.User;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // 참여자 목록
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participation> participants = new HashSet<>();

    // 편의 메서드
    public void addParticipant(Participation participation) {
        participants.add(participation);
        participation.setProject(this);
    }

    public void removeParticipant(Participation participation) {
        participants.remove(participation);
        participation.setProject(null);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}