package com.example.portpilot.domain.portfolio.dto;

import lombok.*;

import javax.validation.constraints.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PortfolioRequest {
    @NotBlank
    private String title;

    private String description;

    @Pattern(regexp = "^(https?://).*$", message = "유효한 URL을 입력하세요")
    private String link;
}
