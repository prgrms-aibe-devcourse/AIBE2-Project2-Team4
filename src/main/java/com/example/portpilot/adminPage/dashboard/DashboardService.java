package com.example.portpilot.adminPage.dashboard;


import com.example.portpilot.adminPage.dashboard.SignupStatDto;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;

    public List<SignupStatDto> getSignupStats(LocalDate start, LocalDate end) {
        return userRepository.countNewUsersByDate(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }
}
