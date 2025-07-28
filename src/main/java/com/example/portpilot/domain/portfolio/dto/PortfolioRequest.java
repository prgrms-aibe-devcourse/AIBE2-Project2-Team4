package com.example.portpilot.domain.portfolio.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioRequest {
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String background;

    private String results;
    private String features;
    private String stages;
    private String details;

    @NotBlank
    private String description;

    private String link;
    private String tags;
    private String category;

    public static PortfolioRequest fromResponse(PortfolioResponse resp) {
        return PortfolioRequest.builder()
                .id(resp.getId())
                .title(resp.getTitle())
                .background(resp.getBackground())
                .results(resp.getResults())
                .features(resp.getFeatures())
                .stages(resp.getStages())
                .details(resp.getDetails())
                .description(resp.getDescription())
                .link(resp.getLink())
                .tags(resp.getTags())
                .category(resp.getCategory())
                .build();
    }
}