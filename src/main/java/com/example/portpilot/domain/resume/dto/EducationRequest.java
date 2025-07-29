package com.example.portpilot.domain.resume.dto;

import com.example.portpilot.domain.resume.entity.EducationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class EducationRequest {
    private String schoolName;
    private EducationType type;
    private String level;
    private String major;
    private String additionalMajor;
    private LocalDate admissionDate;
    private LocalDate graduationDate;
    private Boolean isCurrent = false;
}