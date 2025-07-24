package com.example.portpilot.domain.study;

import com.example.portpilot.domain.study.entity.StudyTechStack;
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

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String techStack;

    private int maxMembers;

    private int backendRecruit;
    private int frontendRecruit;
    private int designerRecruit;
    private int plannerRecruit;

    //기술 스택
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyTechStack> techStacks = new ArrayList<>();

    private LocalDateTime deadline;

    @Column(name = "is_closed", nullable = false)
    private boolean closed;

    private boolean isBlocked;

    public boolean isRecruitmentClosed() {
        return closed || deadline.isBefore(LocalDateTime.now());
    }
}
