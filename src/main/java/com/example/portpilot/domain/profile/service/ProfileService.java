package com.example.portpilot.domain.profile.service;

import com.example.portpilot.domain.profile.dto.ActivityDto;
import com.example.portpilot.domain.profile.dto.ParticipationDto;
import com.example.portpilot.domain.profile.dto.PortfolioDto;
import com.example.portpilot.domain.profile.dto.ProfileDto;
import com.example.portpilot.domain.profile.dto.ProfileStatsDto;
import com.example.portpilot.domain.profile.entity.UserProfile;
import com.example.portpilot.domain.profile.entity.UserSkill;
import com.example.portpilot.domain.profile.repository.ActivityLogRepository;
import com.example.portpilot.domain.profile.repository.UserProfileRepository;
import com.example.portpilot.domain.profile.repository.UserSkillRepository;
import com.example.portpilot.domain.project.entity.ParticipationStatus;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.repository.ParticipationRepository;
import com.example.portpilot.domain.project.repository.ProjectRepository;
import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import com.example.portpilot.domain.portfolio.repository.PortfolioRepository;
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
    private final ActivityLogRepository activityRepo;
    private final ParticipationRepository participationRepo;
    private final ProjectRepository projectRepo;
    private final PortfolioRepository portfolioRepo;

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email);
    }

    /** 프로필 기본 정보 조회 */
    @Transactional
    public ProfileDto getProfile() {
        User user = currentUser();
        UserProfile up = profileRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    p.setPosition("");
                    p.setBio("");
                    return profileRepo.save(p);
                });
        List<String> skills = skillRepo.findAllByUserId(user.getId()).stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());
        return new ProfileDto(
                user.getName(),
                up.getPosition(),
                up.getBio(),
                skills
        );
    }

    /** 최근 활동 조회 */
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

    /** 통계 정보 조회 */
    public ProfileStatsDto getStats() {
        User user = currentUser();
        long ongoing = projectRepo.countByOwnerAndStatus(user, ProjectStatus.OPEN);
        long delivered = projectRepo.countByOwnerAndStatus(user, ProjectStatus.CLOSED);
        ProfileStatsDto.Purchases purchases = new ProfileStatsDto.Purchases(
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.DELIVERED),
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.PENDING_REVIEW),
                portfolioRepo.countByUserIdAndStatus(user.getId(), PortfolioStatus.CANCELLED)
        );
        // 이슈 수는 프로젝트 서비스에서 집계하거나 0으로 둡니다.
        return new ProfileStatsDto(ongoing, delivered, 0, purchases);
    }

    /** 스킬 목록 조회 */
    public List<String> getSkills() {
        User user = currentUser();
        return skillRepo.findAllByUserId(user.getId()).stream()
                .map(UserSkill::getSkillName)
                .collect(Collectors.toList());
    }

    /** 프로필 수정 */
    @Transactional
    public void updateProfile(Long userId,
                              String position,
                              String bio,
                              List<String> newSkills) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId));

        UserProfile up = profileRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });

        up.setPosition(position);
        up.setBio(bio);
        profileRepo.save(up);

        skillRepo.deleteAllByUserId(userId);
        if (newSkills != null) {
            newSkills.stream()
                    .filter(s -> !s.isBlank())
                    .forEach(skillName -> {
                        UserSkill us = new UserSkill();
                        us.setUser(user);
                        us.setSkillName(skillName.trim());
                        skillRepo.save(us);
                    });
        }
    }

    /** 내가 보낸 요청 조회 (PENDING) */
    public List<ParticipationDto> getSentParticipations(Long userId) {
        return participationRepo.findByUserIdAndStatus(userId, ParticipationStatus.PENDING).stream()
                .map(p -> new ParticipationDto(
                        p.getId(),
                        p.getProject().getId(),
                        p.getProject().getTitle(),
                        p.getRequestedAt(),
                        p.getStatus()
                ))
                .collect(Collectors.toList());
    }

    /** 내 포트폴리오 조회 */
    public List<PortfolioDto> getMyPortfolios(Long userId) {
        return portfolioRepo.findByUserId(userId).stream()
                .map(p -> new PortfolioDto(
                        p.getId(),
                        p.getTitle(),
                        p.getDescription(),
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /** 내 포트폴리오 삭제 */
    @Transactional
    public void deletePortfolio(Long userId, Long portfolioId) {
        portfolioRepo.findById(portfolioId).ifPresent(p -> {
            if (p.getUser().getId().equals(userId)) {
                portfolioRepo.delete(p);
            } else {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }
        });
    }
}