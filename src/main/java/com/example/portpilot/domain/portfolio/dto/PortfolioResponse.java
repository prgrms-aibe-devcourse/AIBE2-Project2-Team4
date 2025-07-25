package com.example.portpilot.domain.portfolio.dto;

import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import lombok.*;
import java.util.List;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioResponse {
    Long id;
    String title;
    String description;
    String link;
    String tags;
    String category;
    List<String> images;       // 뷰에서는 반복 처리
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String authorName;
    PortfolioStatus status;
}