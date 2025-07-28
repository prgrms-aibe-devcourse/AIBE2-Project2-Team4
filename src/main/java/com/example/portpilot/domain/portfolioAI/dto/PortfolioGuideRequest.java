package com.example.portpilot.domain.portfolioAI.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioGuideRequest {

    @NotNull(message = "파일은 필수입니다.")
    private MultipartFile file;

    @NotBlank(message = "직무 카테고리는 필수입니다.")
    private String jobCategory;

    @NotBlank(message = "세부 직무는 필수입니다.")
    private String jobSubcategory;
}