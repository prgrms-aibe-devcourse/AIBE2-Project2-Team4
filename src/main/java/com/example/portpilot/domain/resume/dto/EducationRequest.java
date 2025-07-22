package com.example.portpilot.domain.resume.dto;

import com.example.portpilot.domain.resume.entity.EducationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EducationRequest {

    @NotBlank(message = "학교명은 필수입니다.")
    private String schoolName;

    @NotNull(message = "교육기관 타입은 필수입니다.")
    private EducationType type;

    private String level;
    private String major;
    private String additionalMajor;
    private LocalDate admissionDate;
    private LocalDate graduationDate;
    private Boolean isCurrent = false;
}