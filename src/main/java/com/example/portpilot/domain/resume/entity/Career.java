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
@Table(name = "career")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Career extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(length = 100)
    private String department;

    @Column(name = "position_title", length = 100)
    private String positionTitle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @Lob
    private String responsibilities;

    @Column(name = "resignation_reason", length = 500)
    private String resignationReason;

    @Builder
    public Career(Resume resume, String companyName, String department, String positionTitle,
                  LocalDate startDate, LocalDate endDate, Boolean isCurrent,
                  String responsibilities, String resignationReason) {
        this.resume = resume;
        this.companyName = companyName;
        this.department = department;
        this.positionTitle = positionTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent != null ? isCurrent : false;
        this.responsibilities = responsibilities;
        this.resignationReason = resignationReason;
    }

    public void updateInfo(String companyName, String department, String positionTitle,
                           LocalDate startDate, LocalDate endDate, Boolean isCurrent,
                           String responsibilities, String resignationReason) {
        this.companyName = companyName;
        this.department = department;
        this.positionTitle = positionTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.responsibilities = responsibilities;
        this.resignationReason = resignationReason;
    }
}
