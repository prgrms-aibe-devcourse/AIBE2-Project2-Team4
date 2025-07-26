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
@Table(name = "experience")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Experience extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "activity_name", nullable = false, length = 200)
    private String activityName;

    @Column(length = 200)
    private String institution;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @Lob
    private String content;

    @Builder
    public Experience(Resume resume, String activityName, String institution,
                      LocalDate startDate, LocalDate endDate, Boolean isCurrent, String content) {
        this.resume = resume;
        this.activityName = activityName;
        this.institution = institution;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent != null ? isCurrent : false;
        this.content = content;
    }

    public void updateInfo(String activityName, String institution, LocalDate startDate,
                           LocalDate endDate, Boolean isCurrent, String content) {
        this.activityName = activityName;
        this.institution = institution;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.content = content;
    }
}
