package com.example.portpilot.domain.resume.entity;

import com.example.portpilot.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "education")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Education extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "school_name", nullable = false, length = 200)
    private String schoolName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EducationType type;

    @Column(length = 100)
    private String level;

    @Column(length = 100)
    private String major;

    @Column(name = "additional_major", length = 100)
    private String additionalMajor;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @Builder
    public Education(Resume resume, String schoolName, EducationType type, String level,
                     String major, String additionalMajor, LocalDate admissionDate,
                     LocalDate graduationDate, Boolean isCurrent) {
        this.resume = resume;
        this.schoolName = schoolName;
        this.type = type;
        this.level = level;
        this.major = major;
        this.additionalMajor = additionalMajor;
        this.admissionDate = admissionDate;
        this.graduationDate = graduationDate;
        this.isCurrent = isCurrent != null ? isCurrent : false;
    }

    public void updateInfo(String schoolName, EducationType type, String level,
                           String major, String additionalMajor, LocalDate admissionDate,
                           LocalDate graduationDate, Boolean isCurrent) {
        this.schoolName = schoolName;
        this.type = type;
        this.level = level;
        this.major = major;
        this.additionalMajor = additionalMajor;
        this.admissionDate = admissionDate;
        this.graduationDate = graduationDate;
        this.isCurrent = isCurrent;
    }
}
