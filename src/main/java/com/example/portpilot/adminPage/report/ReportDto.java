package com.example.portpilot.adminPage.report;

import com.example.portpilot.adminPage.report.ReportStatus;
import com.example.portpilot.adminPage.report.ReportTargetType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {

    private Long id;

    private Long reporterId;

    private Long reportedUserId;

    private ReportTargetType targetType;

    private Long targetId;

    private String reason;

    private ReportStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    private String reportAdminName; // 처리한 어드민 이름
}
