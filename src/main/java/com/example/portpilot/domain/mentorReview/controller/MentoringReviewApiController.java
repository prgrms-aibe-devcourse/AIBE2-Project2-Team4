package com.example.portpilot.domain.mentorReview.controller;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewCreateDto;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewResponseDto;
import com.example.portpilot.domain.mentorReview.service.MentoringReviewService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentoring-review")
public class MentoringReviewApiController {

    private final MentoringReviewService reviewService;
    private final UserRepository userRepository;

    // 후기 작성
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody MentoringReviewCreateDto dto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            reviewService.createReview(dto, currentUser);
            return ResponseEntity.ok().body(Map.of("message", "후기가 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 모든 후기 조회
    @GetMapping
    public ResponseEntity<List<MentoringReviewResponseDto>> getAllReviews() {
        List<MentoringReviewResponseDto> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // 후기 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MentoringReviewResponseDto> getReviewById(@PathVariable Long id) {
        try {
            MentoringReviewResponseDto review = reviewService.getReviewById(id);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 작성 가능한 멘토링 목록 조회
    @GetMapping("/available-mentorings")
    public ResponseEntity<?> getAvailableMentorings() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<MentoringRequest> mentorings = reviewService.getCompletedMentoringsByUser(currentUser);

        List<Map<String, Object>> response = mentorings.stream()
                .map(mentoring -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", mentoring.getId());
                    map.put("mentorName", mentoring.getMentorName());
                    map.put("topic", mentoring.getTopic());
                    map.put("scheduledAt", mentoring.getScheduledAt());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 현재 사용자 조회
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByEmail(auth.getName());
    }
}