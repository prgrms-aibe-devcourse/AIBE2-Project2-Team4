
package com.example.portpilot.domain.job.entity;

import com.example.portpilot.domain.company.entity.Company;
import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_position")
@Getter
@Setter
@NoArgsConstructor
public class JobPosition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "salary", length = 100)
    private String salary;

    @Column(name = "location", length = 300)
    private String location;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "모집중";
}