package com.example.portpilot.domain.profile.service;

import com.example.portpilot.domain.portfolio.PortfolioRepository;
import com.example.portpilot.domain.portfolio.PortfolioStatus;
import com.example.portpilot.domain.profile.dto.ActivityDto;
import com.example.portpilot.domain.profile.dto.ProfileDto;
import com.example.portpilot.domain.profile.dto.ProfileStatsDto;
import com.example.portpilot.domain.profile.entity.UserProfile;
import com.example.portpilot.domain.profile.entity.UserSkill;
import com.example.portpilot.domain.profile.repository.ActivityLogRepository;
import com.example.portpilot.domain.profile.repository.UserProfileRepository;
import com.example.portpilot.domain.profile.repository.UserSkillRepository;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.repository.ProjectRepository;
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
     * – 데이터가 없으면 빈 프로필을 생성해서 저장 후 반환
     */
    @Transactional
    public ProfileDto getProfile() {
        User u = currentUser();

        UserProfile up = profileRepo.findByUserId(u.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(u);
                    p.setPosition("");
                    p.setBio("");
                    return profileRepo.save(p);
                });

        List<String> skills = skillRepo.findAllByUserId(u.getId()).stream()
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
     * 통계 정보 조회
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
        return activityRepo.findByUserIdOrderByDateDesc(user.getId()).stream()
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
        User user = currentUser();
        return skillRepo.findAllByUserId(user.getId()).stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());
    }

    /**
     * 프로필 수정 (포지션·소개·스킬)
     */
    @Transactional
    public void updateProfile(String position, String bio, List<String> newSkills) {
        User user = currentUser();

        // 프로필 저장(생성 혹은 업데이트)
        UserProfile profile = profileRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });
        profile.setPosition(position);
        profile.setBio(bio);
        profileRepo.save(profile);

        // 스킬 리프레시
        skillRepo.deleteAllByUserId(user.getId());

        if (newSkills != null) {
            for (String skillName : newSkills) {
                if (!skillName.isBlank()) {
                    UserSkill skill = new UserSkill();
                    skill.setUser(user);
                    skill.setSkillName(skillName.trim());
                    skillRepo.save(skill);
                }
            }
        }
    }
}
