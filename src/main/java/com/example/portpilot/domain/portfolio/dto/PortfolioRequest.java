package com.example.portpilot.domain.portfolio.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioRequest {
    private Long id;               // 수정 시 필요
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private String link;
    private String tags;
    private String category;

    public static PortfolioRequest fromResponse(PortfolioResponse resp) {
        return PortfolioRequest.builder()
                .id(resp.getId())
                .title(resp.getTitle())
                .description(resp.getDescription())
                .link(resp.getLink())
                .tags(resp.getTags())
                .category(resp.getCategory())
                .build();
    }
}