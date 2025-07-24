package com.example.portpilot.adminPage.dashboard;


import com.example.portpilot.adminPage.dashboard.SignupStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/signup-stats")
    public List<SignupStatDto> getSignupStats(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,

            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return dashboardService.getSignupStats(start, end);
    }


    @GetMapping("/withdraw-stats")
    public List<WithdrawStatDto> getWithdrawStats(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return dashboardService.getWithdrawStats(start, end);
    }

    @GetMapping("/matching-status")
    public List<MatchingStatusDto> getMatchingStatus() {
        return dashboardService.getMatchingStatusCounts();
    }

    @GetMapping("/unresolved-report-count")
    public int getUnresolvedReportCount() {
        return dashboardService.getUnresolvedReportCount();
    }

}
