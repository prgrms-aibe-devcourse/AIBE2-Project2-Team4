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
    public ResponseEntity<List<MentorProfileResponseDto>> getMentors() {
        List<MentorProfile> mentors = mentorProfileRepository.findAll();
        List<MentorProfileResponseDto> dtos = mentors.stream()
                .map(mentor -> new MentorProfileResponseDto(
                        mentor.getUser().getName(),
                        mentor.getUser().getEmail(),
                        mentor.getTechStack(),
                        mentor.getDescription()
                )).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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
        return ResponseEntity.ok(MentoringRequestResponseDto.fromEntity(mentoringService.findById(id)));
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
    @PostMapping("/mentoring/{id}/propose")
    public ResponseEntity<?> propose(@PathVariable Long id, @RequestBody ScheduleDto dto, @AuthenticationPrincipal User currentUser) {
        mentoringService.proposeSchedule(id, dto.getProposedAt(), currentUser);
        return ResponseEntity.ok().build();
    }

    // 일정 확정
    @PostMapping("/mentoring/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
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

    // 현재 로그인한 사용자 조회
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) return null;
        return userRepository.findByEmail(auth.getName());
    }
}
