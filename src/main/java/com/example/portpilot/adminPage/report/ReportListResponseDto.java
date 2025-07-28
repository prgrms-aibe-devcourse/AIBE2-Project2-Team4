package com.example.portpilot.adminPage.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReportListResponseDto {
    private List<ReportDetailDto> reports;
    private int totalPages;
}
