package com.example.portpilot.domain.portfolio.dto;

import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioResponse {
    private Long id;
    private String title;
    private String background;
    private String results;
    private String features;
    private String stages;
    private String details;
    private String description;
    private String link;
    private String tags;
    private String category;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private PortfolioStatus status;
}