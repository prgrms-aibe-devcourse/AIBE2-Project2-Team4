package com.example.portpilot.domain.profile;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepo;
    private final UserSkillRepository skillRepo;
    // private final ProjectRepository projectRepo;
    // private final PortfolioRepository portfolioRepo;
    // private final ActivityLogRepository activityRepo;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    public ProfileDto getProfile() {
        User u = currentUser();
        UserProfile up = profileRepo.findByUserId(u.getId());
        List<String> skills = skillRepo.findAllByUserId(u.getId())
                .stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());

        return new ProfileDto(u.getName(),
                up.getPosition(),
                up.getBio(),
                skills);
    }

    public ProfileStatsDto getStats() {
        // TODO: ProjectRepository / PortfolioRepository
        long ongoing   = 0;
        long delivered= 0;
        long issues   = 0;
        ProfileStatsDto.Purchases p = new ProfileStatsDto.Purchases(
                0,  // countPurchased
                0,  // countPendingReviews
                0   // countCancelled
        );
        return new ProfileStatsDto(ongoing, delivered, issues, p);
    }

    private final ActivityLogRepository activityRepo;

    public List<ActivityDto> getRecentActivity(int size) {
        User user = currentUser();
        return activityRepo.findByUserIdOrderByDateDesc(user.getId()).stream()
                .limit(size)
                .map(log -> new ActivityDto(
                        log.getDate(),
                        log.getProjectName(),
                        log.getRole(),
                        log.getStatus()))
                .collect(Collectors.toList());
    }
}


