package com.example.portpilot.domain.study.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_recruitment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyRecruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 (EAGER 로딩)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String techStack;

    private int maxMembers;
    private int backendRecruit;
    private int frontendRecruit;
    private int designerRecruit;
    private int plannerRecruit;

    // 기술 스택 리스트 (EAGER 로딩)
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<StudyTechStack> techStacks = new ArrayList<>();

    // 참여자 리스트
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyParticipation> participations = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime deadline;

    // 모집 마감 여부
    @Column(name = "closed", nullable = false)
    @Builder.Default
    private boolean closed = false;

    // 스터디 활동 종료 여부 (실제 종료 의미)
    @Column(name = "completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    // 관리자에 의해 숨겨진 스터디 여부
    @Builder.Default
    private boolean isBlocked = false;

    // 모집 마감 여부 판단 (프론트에서 활용)
    public boolean isRecruitmentClosed() {
        return closed || deadline.isBefore(LocalDateTime.now());
    }

    // 기술스택 1개 추가
    public void addTechStack(JobType jobType, String techName) {
        StudyTechStack techStack = StudyTechStack.builder()
                .study(this)
                .jobType(jobType)
                .techStack(techName)
                .build();
        this.techStacks.add(techStack);
    }

    // 기술스택 일괄 추가
    public void addTechStacks(JobType jobType, List<String> techNames) {
        if (techNames != null && !techNames.isEmpty()) {
            techNames.forEach(name -> addTechStack(jobType, name));
        }
    }

    // 스터디 종료 상태 설정
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // 종료 여부 확인용 헬퍼 메서드 (필요 시)
    public boolean isStudyDone() {
        return completed;
    }
}
