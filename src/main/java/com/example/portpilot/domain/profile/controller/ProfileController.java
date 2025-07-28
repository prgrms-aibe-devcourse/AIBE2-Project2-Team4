package com.example.portpilot.domain.profile.controller;

import com.example.portpilot.domain.profile.dto.*;
import com.example.portpilot.domain.profile.service.ProfileService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService service;
    private final UserRepository   userRepository;

    // SecurityContext 에서 이메일 가져와서 User 엔티티 ID 리턴
    private Long currentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User u = userRepository.findByEmail(email);
        if (u == null) {
            throw new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다: " + email);
        }
        return u.getId();
    }

    /** 1) 내 프로필 */
    @GetMapping
    public ResponseEntity<ProfileDto> getProfile() {
        return ResponseEntity.ok(service.getProfile());
    }

    /** 2) 내 프로필 통계 */
    @GetMapping("/stats")
    public ResponseEntity<ProfileStatsDto> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    /** 3) 최근 활동 (기본 5개) */
    @GetMapping("/activity")
    public ResponseEntity<List<ActivityDto>> getActivity(
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(service.getRecentActivity(size));
    }

    /** 4) 스킬 목록 */
    @GetMapping("/skills")
    public ResponseEntity<List<String>> getSkills() {
        return ResponseEntity.ok(service.getSkills());
    }

    /** 5) 프로필 수정 */
    @PutMapping
    public ResponseEntity<Void> updateProfile(
            @RequestBody ProfileUpdateRequest req) {
        service.updateProfile(
                currentUserId(),
                req.getPosition(),
                req.getBio(),
                req.getSkills()
        );
        return ResponseEntity.ok().build();
    }

    /** 6) 내가 보낸 참여 요청 (PENDING) */
    @GetMapping("/participations/sent")
    public ResponseEntity<List<ParticipationDto>> getSentParticipations() {
        return ResponseEntity.ok(
                service.getSentParticipations(currentUserId())
        );
    }

    /** 7) 내 포트폴리오 목록 */
    @GetMapping("/portfolios")
    public ResponseEntity<List<PortfolioDto>> getPortfolios() {
        return ResponseEntity.ok(
                service.getMyPortfolios(currentUserId())
        );
    }

    /** 8) 포트폴리오 삭제 */
    @DeleteMapping("/portfolios/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        service.deletePortfolio(currentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}