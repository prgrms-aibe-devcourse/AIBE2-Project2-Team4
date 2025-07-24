package com.example.portpilot.domain.study.entity;

import com.example.portpilot.domain.study.StudyRecruitment;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "study_tech_stack")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private StudyRecruitment study;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;  // BACKEND, FRONTEND, DESIGNER, PLANNER

    @Column(nullable = false)
    private String techStack;  // Java, Spring, React, Figma 등
}
