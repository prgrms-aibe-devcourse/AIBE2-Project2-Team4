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

    // 작성자 - EAGER로 설정해서 확실하게 로딩
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

    // 기술 스택 - EAGER로 확실하게 로딩
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<StudyTechStack> techStacks = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "closed", nullable = false)
    @Builder.Default
    private boolean closed = false;

    @Builder.Default
    private boolean isBlocked = false;

    // 모집 마감 여부 체크
    public boolean isRecruitmentClosed() {
        return closed || deadline.isBefore(LocalDateTime.now());
    }

    // 기술스택 추가 편의 메서드
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
}