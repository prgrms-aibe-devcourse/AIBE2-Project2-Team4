// src/main/java/com/example/portpilot/domain/profile/ProfileService.java
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
import org.springframework.transaction.annotation.Transactional;

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
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 프로필 정보 조회
     */
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

    /**
     * 사용자 통계 정보 조회
     */
    public ProfileStatsDto getStats() {
        User user = currentUser();

        long ongoing = projectRepo.countByOwnerAndStatus(user, ProjectStatus.OPEN);
        long delivered = projectRepo.countByOwnerAndStatus(user, ProjectStatus.CLOSED);

        ProfileStatsDto.Purchases purchases = new ProfileStatsDto.Purchases(
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.DELIVERED),
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.PENDING_REVIEW),
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.CANCELLED)
        );

        return new ProfileStatsDto(ongoing, delivered, 0, purchases);
    }

    /**
     * 최근 활동 내역 조회
     */
    public List<ActivityDto> getRecentActivity(int size) {
        User user = currentUser();
        return activityRepo.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .limit(size)
                .map(log -> new ActivityDto(
                        log.getDate(),
                        log.getProjectName(),
                        log.getRole(),
                        log.getStatus()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 현재 사용자 스킬 목록 조회
     */
    public List<String> getSkills() {
        return skillRepo.findAllByUserId(currentUser().getId())
                .stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateProfile(String position, String bio, List<String> newSkills) {
        User user = currentUser();

        // 1) 프로필 저장
        UserProfile profile = profileRepo.findByUserId(user.getId());
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }
        profile.setPosition(position);
        profile.setBio(bio);
        profileRepo.save(profile);

        // 2) 기존 스킬 일괄 삭제 (트랜잭션 내에서 실행)
        skillRepo.deleteAllByUserId(user.getId());

        // 3) 새 스킬들 저장
        for (String skillName : newSkills) {
            if (skillName != null && !skillName.isBlank()) {
                UserSkill skill = new UserSkill();
                skill.setUser(user);
                skill.setSkillName(skillName.trim());
                skillRepo.save(skill);
            }
        }
    }
}
