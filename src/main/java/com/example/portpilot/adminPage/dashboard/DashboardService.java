package com.example.portpilot.adminPage.dashboard;


import com.example.portpilot.adminPage.dashboard.SignupStatDto;
import com.example.portpilot.adminPage.report.ReportRepository;
import com.example.portpilot.adminPage.report.ReportStatus;
import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.mentorRequest.repository.MentoringRequestRepository;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final ReportRepository reportRepository;


    public List<SignupStatDto> getSignupStats(LocalDate start, LocalDate end) {
        return userRepository.countNewUsersByDate(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    public List<WithdrawStatDto> getWithdrawStats(LocalDate start, LocalDate end) {
        return userRepository.countWithdrawnUsersByDate(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    public List<MatchingStatusDto> getMatchingStatusCounts() {
        return Arrays.stream(MentoringStatus.values())
                .map(status -> new MatchingStatusDto(
                        status.name(),
                        mentoringRequestRepository.countByStatus(status)
                ))
                .collect(Collectors.toList());
    }

    public int getUnresolvedReportCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }


}
