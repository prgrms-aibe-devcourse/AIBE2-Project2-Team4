package com.example.portpilot.domain.resume.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CareerRequest {

    @NotBlank(message = "회사명은 필수입니다.")
    private String companyName;    private String department;

    private String positionTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent = false;
    private String responsibilities;
    private String resignationReason;
}