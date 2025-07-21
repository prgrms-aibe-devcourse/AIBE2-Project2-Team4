package com.example.portpilot.domain.profile;

import com.example.portpilot.domain.project.ProjectStatus;
import com.example.portpilot.domain.project.ProjectRepository;
import com.example.portpilot.domain.portfolio.PortfolioRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepo;
    private final UserSkillRepository skillRepo;
    private final ProjectRepository projectRepo;
    private final PortfolioRepository portfolioRepo;
    private final ActivityLogRepository activityRepo;

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
        User u = currentUser();
        long ongoing   = projectRepo.countByOwnerAndStatus(u, ProjectStatus.OPEN);
        long delivered= portfolioRepo.countDeliveredByUser(u.getId());
        long issues   = portfolioRepo.countIssuesByUser(u.getId());

        ProfileStatsDto.Purchases p = new ProfileStatsDto.Purchases(
                portfolioRepo.countPurchased(u.getId()),
                portfolioRepo.countPendingReviews(u.getId()),
                portfolioRepo.countCancelled(u.getId())
        );
        return new ProfileStatsDto(ongoing, delivered, issues, p);
    }

    public List<ActivityDto> getRecentActivity(int size) {
        User u = currentUser();
        return activityRepo.findTopNByUserIdOrderByDateDesc(u.getId(), size)
                .stream()
                .map(log -> new ActivityDto(
                        log.getDate(), log.getProjectName(),
                        log.getRole(), log.getStatus()))
                .collect(Collectors.toList());
    }
}
