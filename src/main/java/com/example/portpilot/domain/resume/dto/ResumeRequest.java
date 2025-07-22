package com.example.portpilot.domain.resume.dto;

import com.example.portpilot.domain.resume.entity.ResumeStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class ResumeRequest {

    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    private String industry;
    private String position;
    private String targetCompany;
    private String highlights;
    private ResumeStatus status;
}
