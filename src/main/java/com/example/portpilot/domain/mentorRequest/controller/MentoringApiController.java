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

    //멘토 등록
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

    //멘토링 신청
    @PostMapping
    public ResponseEntity<?> requestMentoring(@RequestBody MentoringRequestCreateDto dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        mentoringService.requestMentoring(dto, user);
        return ResponseEntity.ok().build();
    }

    //멘토 리스트 불러오기
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

    //
    @GetMapping("/requests/received")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequestsAsMentor() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<MentoringRequestResponseDto> result = mentoringService.getRequestsByMentor(user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequestsAsMentee() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<MentoringRequestResponseDto> result = mentoringService.getRequestsByUser(user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests/accepted-or-completed")
    public ResponseEntity<List<MentoringRequestResponseDto>> getAcceptedAndCompletedRequests() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<MentoringRequestResponseDto> result = mentoringService.getCompletedOrAcceptedRequests(user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<MentoringRequestResponseDto> getRequestById(@PathVariable Long id) {
        MentoringRequest request = mentoringService.findById(id);
        return ResponseEntity.ok(MentoringRequestResponseDto.fromEntity(request));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptMentoring(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        mentoringService.acceptMentoringOnly(id, currentUser);
        return ResponseEntity.ok().body(Map.of("message", "수락 완료"));
    }

    @PostMapping("/mentoring/{id}/propose")
    public ResponseEntity<?> propose(@PathVariable Long id,
                                     @RequestBody ScheduleDto dto,
                                     @AuthenticationPrincipal User currentUser) {
        mentoringService.proposeSchedule(id, dto.getProposedAt(), currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mentoring/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id,
                                     @AuthenticationPrincipal User currentUser) {
        mentoringService.confirmSchedule(id, currentUser);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/schedule")
    public ResponseEntity<?> scheduleMentoring(
            @PathVariable Long id,
            @RequestBody MentoringScheduleDto dto,
            @AuthenticationPrincipal User currentUser) {
        mentoringService.scheduleMentoring(id, dto.getScheduledAt(), currentUser);
        return ResponseEntity.ok().body(Map.of("message", "일정 확정됨"));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName());
        mentoringService.updateStatus(id, MentoringStatus.REJECTED, currentUser);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String email = auth.getName();
        return userRepository.findByEmail(email);
    }
}
