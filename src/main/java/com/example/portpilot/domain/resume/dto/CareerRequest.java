package com.example.portpilot.domain.resume.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CareerRequest {

    @Size(max = 200, message = "회사명은 200자 이하여야 합니다.")
    private String companyName;

    private String department;
    private String positionTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent = false;
    private String responsibilities;
    private String resignationReason;
}