package com.example.portpilot.adminPage.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportDetailDto {
    private Long id;
    private String reporterName;
    private String targetSummary;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
