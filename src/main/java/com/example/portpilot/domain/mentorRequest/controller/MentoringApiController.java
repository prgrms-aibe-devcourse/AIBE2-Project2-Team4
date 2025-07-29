package com.example.portpilot.domain.mentorRequest.controller;

import com.example.portpilot.domain.mentorRequest.Service.MentoringService;
import com.example.portpilot.domain.mentorRequest.dto.*;
import com.example.portpilot.domain.mentorRequest.entity.MentorProfile;
import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.mentorRequest.repository.MentorProfileRepository;
import com.example.portpilot.domain.mentorRequest.repository.MentoringRequestRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentoring")
public class MentoringApiController {

    private final MentoringService mentoringService;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    // 멘토 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerMentor(@RequestBody MentorProfileDto dto) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        try {
            mentoringService.registerMentor(dto, user);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 멘토링 신청
    @PostMapping
    public ResponseEntity<?> requestMentoring(@RequestBody MentoringRequestCreateDto dto) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        mentoringService.requestMentoring(dto, user);
        return ResponseEntity.ok().build();
    }

    // 멘토 목록 조회
    @GetMapping("/mentors")
    public ResponseEntity<Map<String, Object>> getMentors() {
        User currentUser = getCurrentUser();

        List<MentorProfile> mentors = mentorProfileRepository.findAll();
        List<MentorProfileResponseDto> dtos = mentors.stream()
                .map(mentor -> new MentorProfileResponseDto(
                        mentor.getUser().getName(),
                        mentor.getUser().getEmail(),
                        mentor.getTechStack(),
                        mentor.getDescription()
                )).collect(Collectors.toList());

        // Map으로 현재 사용자 이메일도 함께 리턴
        Map<String, Object> result = new HashMap<>();
        result.put("mentors", dtos);
        result.put("currentUserEmail", currentUser != null ? currentUser.getEmail() : null);

        return ResponseEntity.ok(result);
    }

    // 멘토 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<?> updateMentorProfile(@RequestBody MentorProfileDto dto) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        try {
            Optional<Object> existing = mentorProfileRepository.findByUser(user);
            if (!existing.isPresent()) {
                return ResponseEntity.badRequest().body("등록된 멘토 프로필이 없습니다.");
            }

            if (existing.get() instanceof MentorProfile) {
                MentorProfile profile = (MentorProfile) existing.get();
                profile.setTechStack(dto.getTechStack());
                profile.setDescription(dto.getDescription());
                mentorProfileRepository.save(profile);
                return ResponseEntity.ok().body("멘토 프로필이 수정되었습니다.");
            }

            return ResponseEntity.badRequest().body("프로필을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 중 오류가 발생했습니다.");
        }
    }

    // 멘토 프로필 삭제
    @DeleteMapping("/profile")
    public ResponseEntity<?> deleteMentorProfile() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        try {
            Optional<Object> existing = mentorProfileRepository.findByUser(user);
            if (!existing.isPresent()) {
                return ResponseEntity.badRequest().body("등록된 멘토 프로필이 없습니다.");
            }

            if (existing.get() instanceof MentorProfile) {
                MentorProfile profile = (MentorProfile) existing.get();
                mentorProfileRepository.delete(profile);
                return ResponseEntity.ok().body("멘토 프로필이 삭제되었습니다.");
            }

            return ResponseEntity.badRequest().body("프로필을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류가 발생했습니다.");
        }
    }

    // 멘토가 받은 신청 조회
    @GetMapping("/requests/received")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequestsAsMentor() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(mentoringService.getRequestsByMentor(user));
    }

    // 멘티가 보낸 신청 조회
    @GetMapping("/requests/sent")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequestsAsMentee() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(mentoringService.getRequestsByUser(user));
    }

    // 수락된 멘토링 조회
    @GetMapping("/requests/accepted")
    public ResponseEntity<List<MentoringRequestResponseDto>> getAcceptedRequests() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(mentoringService.getAcceptedRequests(user));
    }

    // 완료된 멘토링 조회
    @GetMapping("/requests/completed")
    public ResponseEntity<List<MentoringRequestResponseDto>> getCompletedRequests() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(mentoringService.getCompletedRequests(user));
    }

    // 특정 요청 상세 조회
    @GetMapping("/requests/{id}")
    public ResponseEntity<MentoringRequestResponseDto> getRequestById(@PathVariable Long id) {
        MentoringRequest entity = mentoringService.findById(id);
        MentoringRequestResponseDto dto = MentoringRequestResponseDto.fromEntity(entity);

        User currentUser = getCurrentUser(); // 로그인 유저 가져오기
        if (currentUser != null) {
            dto.setCurrentUserId(currentUser.getId());
        }

        return ResponseEntity.ok(dto);
    }

    // 멘토가 신청 수락
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptMentoring(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        mentoringService.acceptMentoringOnly(id, currentUser);
        return ResponseEntity.ok().body(Map.of("message", "수락 완료"));
    }

    // 일정 제안
    @PostMapping("/requests/{id}/propose")
    public ResponseEntity<?> propose(@PathVariable Long id, @RequestBody ScheduleDto dto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        mentoringService.proposeSchedule(id, dto.getProposedAt(), currentUser);
        return ResponseEntity.ok().build();
    }

    // 일정 확정
    @PostMapping("/requests/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        mentoringService.confirmSchedule(id, currentUser);
        return ResponseEntity.ok().build();
    }

    // 일정 저장 또는 확정
    @PostMapping("/{id}/schedule")
    public ResponseEntity<?> scheduleMentoring(@PathVariable Long id, @RequestBody MentoringScheduleDto dto, @AuthenticationPrincipal User currentUser) {
        mentoringService.scheduleMentoring(id, dto.getScheduledAt(), currentUser);
        return ResponseEntity.ok().body(Map.of("message", "일정 확정됨"));
    }

    // 멘토링 신청 거절
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName());
        mentoringService.updateStatus(id, MentoringStatus.REJECTED, currentUser);
        return ResponseEntity.ok().build();
    }

    // 정식 멘토링 완료 처리 (멘토/멘티 둘 다 동의 필요한 버전)
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeMentoring(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            mentoringService.completeMentoring(id, currentUser);
            return ResponseEntity.ok().body(Map.of("message", "멘토링 완료 요청이 전송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 현재 로그인한 사용자 조회
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) return null;
        return userRepository.findByEmail(auth.getName());
    }
}
