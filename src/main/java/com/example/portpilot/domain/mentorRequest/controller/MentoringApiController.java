package com.example.portpilot.domain.mentorRequest.controller;

import com.example.portpilot.domain.mentorRequest.Service.MentoringService;
import com.example.portpilot.domain.mentorRequest.dto.MentorProfileDto;
import com.example.portpilot.domain.mentorRequest.dto.MentorProfileResponseDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringRequestCreateDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringRequestResponseDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringScheduleDto;
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
    private final MentoringRequestRepository mentoringRequestRepository;

    // 1. 멘토 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerMentor(@RequestBody MentorProfileDto dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            mentoringService.registerMentor(dto, user);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 멘토링 신청
    @PostMapping
    public ResponseEntity<?> requestMentoring(@RequestBody MentoringRequestCreateDto dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        mentoringService.requestMentoring(dto, user);
        return ResponseEntity.ok().build();
    }

    // 3. 멘토 목록 조회
    @GetMapping("/mentors")
    public ResponseEntity<List<MentorProfileResponseDto>> getMentors() {
        List<MentorProfile> mentors = mentorProfileRepository.findAll();

        List<MentorProfileResponseDto> dtos = mentors.stream()
                .map(mentor -> new MentorProfileResponseDto(
                        mentor.getUser().getName(),
                        mentor.getUser().getEmail(),
                        mentor.getTechStack(),
                        mentor.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 4-1. 내가 멘토로 받은 신청 목록
    @GetMapping("/requests/received")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequestsAsMentor() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MentoringRequest> list = mentoringRequestRepository.findByMentor(user);
        List<MentoringRequestResponseDto> dtos = list.stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 4-2. 내가 멘티로 신청한 내역
    @GetMapping("/requests/sent")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequestsAsMentee() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MentoringRequestResponseDto> result = mentoringService.getRequestsByUser(user);
        return ResponseEntity.ok(result);
    }

    // 4-3. 내가 참여한 완료/수락된 멘토링 내역 (멘토 or 멘티)
    @GetMapping("/requests/accepted-or-completed")
    public ResponseEntity<List<MentoringRequestResponseDto>> getAcceptedAndCompletedRequests() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MentoringStatus> statuses = Arrays.asList(MentoringStatus.ACCEPTED, MentoringStatus.COMPLETED);

        List<MentoringRequest> asMentor = mentoringRequestRepository.findByMentorAndStatusIn(user, statuses);
        List<MentoringRequest> asMentee = mentoringRequestRepository.findByUserAndStatusIn(user, statuses);

        Set<MentoringRequest> all = new HashSet<>();
        all.addAll(asMentor);
        all.addAll(asMentee);

        List<MentoringRequestResponseDto> dtos = all.stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 5. 상세 조회
    @GetMapping("/requests/{id}")
    public ResponseEntity<MentoringRequestResponseDto> getRequestById(@PathVariable Long id) {
        MentoringRequest request = mentoringService.findById(id);
        return ResponseEntity.ok(MentoringRequestResponseDto.fromEntity(request));
    }

    // 6. 수락
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptMentoring(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        mentoringService.acceptMentoringOnly(id, currentUser);
        return ResponseEntity.ok().body(Map.of("message", "수락 완료"));
    }

    // 7. 일정 확정
    @PostMapping("/{id}/schedule")
    public ResponseEntity<?> scheduleMentoring(
            @PathVariable Long id,
            @RequestBody MentoringScheduleDto dto,
            @AuthenticationPrincipal User currentUser) {

        mentoringService.scheduleMentoring(id, dto.getScheduledAt());
        return ResponseEntity.ok().body(Map.of("message", "일정 확정됨"));
    }

    // 8. 거절
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName());
        mentoringService.updateStatus(id, MentoringStatus.REJECTED, currentUser);
        return ResponseEntity.ok().build();
    }

    // 현재 로그인 유저 가져오기
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String email = auth.getName(); // 로그인 ID가 이메일이면
        return userRepository.findByEmail(email);
    }
}
