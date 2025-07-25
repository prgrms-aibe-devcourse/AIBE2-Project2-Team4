package com.example.portpilot.domain.portfolio.dto;

import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponse {
    private Long id;
    private String title;
    private String description;
    private String link;
    private LocalDateTime createdAt;
    private PortfolioStatus status;
}