package com.example.portpilot.domain.resume.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String industry;

    @Column(length = 100)
    private String position;

    @Column(name = "target_company", length = 200)
    private String targetCompany;

    @Lob
    private String highlights;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeStatus status;

    // 연관관계 매핑
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @Builder
    public Resume(User user, String title, String industry, String position,
                  String targetCompany, String highlights, ResumeStatus status) {
        this.user = user;
        this.title = title;
        this.industry = industry;
        this.position = position;
        this.targetCompany = targetCompany;
        this.highlights = highlights;
        this.status = status;
    }

    // 비즈니스 메서드
    public void updateBasicInfo(String title, String industry, String position, String targetCompany, String highlights) {
        this.title = title;
        this.industry = industry;
        this.position = position;
        this.targetCompany = targetCompany;
        this.highlights = highlights;
    }

    public void updateStatus(ResumeStatus status) {
        this.status = status;
    }
}