package com.example.portpilot.domain.mentorRequest.controller;

import com.example.portpilot.domain.mentorRequest.Service.MentoringService;
import com.example.portpilot.domain.mentorRequest.dto.MentorProfileDto;
import com.example.portpilot.domain.mentorRequest.dto.MentorProfileResponseDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringRequestCreateDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringRequestResponseDto;
import com.example.portpilot.domain.mentorRequest.entity.MentorProfile;
import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.mentorRequest.repository.MentorProfileRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    // 멘토링 신청 (예시)
    @PostMapping
    public ResponseEntity<?> requestMentoring(@RequestBody MentoringRequestCreateDto dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        mentoringService.requestMentoring(dto, user);
        return ResponseEntity.ok().build();
    }
    //멘토 목록 출력
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

    // 멘토링 신청 내역 조회
    @GetMapping("/requests")
    public ResponseEntity<List<MentoringRequestResponseDto>> getRequests() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MentoringRequestResponseDto> result = mentoringService.getRequestsByUser(user);
        return ResponseEntity.ok(result);
    }

    //신청내역 상세보기
    @GetMapping("/requests/{id}")
    public ResponseEntity<MentoringRequestResponseDto> getRequestById(@PathVariable Long id) {
        MentoringRequest request = mentoringService.findById(id);
        return ResponseEntity.ok(MentoringRequestResponseDto.fromEntity(request));
    }

    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName());
        mentoringService.updateStatus(id, MentoringStatus.ACCEPTED, currentUser);
        return ResponseEntity.ok().build();
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

        String email = auth.getName(); // 로그인 ID가 이메일이면
        return userRepository.findByEmail(email);
    }
}
