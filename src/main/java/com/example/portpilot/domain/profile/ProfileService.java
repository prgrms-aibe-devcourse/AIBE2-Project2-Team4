package com.example.portpilot.domain.profile;

import com.example.portpilot.domain.portfolio.PortfolioRepository;
import com.example.portpilot.domain.portfolio.PortfolioStatus;
import com.example.portpilot.domain.project.ProjectRepository;
import com.example.portpilot.domain.project.ProjectStatus;
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

        if (up == null) {
            throw new IllegalStateException("프로필 정보가 없습니다.");
        }

        List<String> skills = skillRepo.findAllByUserId(u.getId())
                .stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());

        return new ProfileDto(
                u.getName(),
                up.getPosition(),
                up.getBio(),
                skills
        );
    }

    public ProfileStatsDto getStats() {
        User user = currentUser();

        long ongoing = projectRepo.countByOwnerAndStatus(user, ProjectStatus.OPEN);
        long delivered = projectRepo.countByOwnerAndStatus(user, ProjectStatus.CLOSED);
        long issues = 0; // 별도 이슈 상태 없으므로 임시 0

        ProfileStatsDto.Purchases purchases = new ProfileStatsDto.Purchases(
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.DELIVERED),
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.PENDING_REVIEW),
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.CANCELLED)
        );

        return new ProfileStatsDto(ongoing, delivered, issues, purchases);
    }

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

    public List<String> getSkills() {
        User user = currentUser();
        return skillRepo.findAllByUserId(user.getId())
                .stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());
    }
    public void updateProfile(String position, String bio) {
        User user = currentUser();
        UserProfile profile = profileRepo.findByUserId(user.getId());

        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        profile.setPosition(position);
        profile.setBio(bio);
        profileRepo.save(profile);
    }

    public void addSkill(String skillName) {
        if (skillName == null || skillName.isBlank()) return;

        User user = currentUser();
        UserSkill skill = new UserSkill();
        skill.setUser(user);
        skill.setSkillName(skillName.trim());
        skillRepo.save(skill);
    }

    public void deleteSkill(String skillName) {
        User user = currentUser();
        List<UserSkill> skills = skillRepo.findAllByUserId(user.getId());

        for (UserSkill skill : skills) {
            if (skill.getSkillName().equalsIgnoreCase(skillName)) {
                skillRepo.delete(skill);
                break;
            }
        }
    }


}



