package com.example.portpilot.domain.mentorReview.controller;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewCreateDto;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewResponseDto;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewUpdateDto;
import com.example.portpilot.domain.mentorReview.service.MentoringReviewService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentoring-review")
public class MentoringReviewApiController {

    private final MentoringReviewService reviewService;
    private final UserRepository userRepository;

    // 후기 등록
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody MentoringReviewCreateDto dto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            reviewService.createReview(dto, currentUser);
            return ResponseEntity.ok(Map.of("message", "후기가 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 전체 후기 목록 조회
    @GetMapping
    public ResponseEntity<List<MentoringReviewResponseDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // 특정 후기 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MentoringReviewResponseDto> getReviewById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reviewService.getReviewById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 후기를 작성할 수 있는 멘토링 목록 조회 (완료 상태 + 미작성)
    @GetMapping("/available-mentorings")
    public ResponseEntity<?> getAvailableMentorings() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        List<MentoringRequest> mentorings = reviewService.getCompletedMentoringsByUser(currentUser);

        List<Map<String, Object>> response = mentorings.stream().map(mentoring -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mentoring.getId());
            map.put("mentorName", mentoring.getMentorName());
            map.put("topic", mentoring.getTopic());
            map.put("scheduledAt", mentoring.getScheduledAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 후기 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id,
                                          @RequestBody MentoringReviewUpdateDto dto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            reviewService.updateReview(id, dto, currentUser);
            return ResponseEntity.ok(Map.of("message", "후기가 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 후기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            reviewService.deleteReview(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "후기가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 특정 멘토가 받은 후기 목록 조회 (페이징)
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<MentoringReviewResponseDto>> getReviewsByMentor(
            @PathVariable Long mentorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<MentoringReviewResponseDto> reviews = reviewService.getReviewsByMentor(mentorId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 특정 멘티가 작성한 후기 목록 조회 (페이징)
    @GetMapping("/mentee/{menteeId}")
    public ResponseEntity<List<MentoringReviewResponseDto>> getReviewsByMentee(
            @PathVariable Long menteeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<MentoringReviewResponseDto> reviews = reviewService.getReviewsByMentee(menteeId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 전체 후기 목록 페이징 조회
    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getAllReviewsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MentoringReviewResponseDto> reviewPage = reviewService.getAllReviewsPaged(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewPage.getContent());
        response.put("currentPage", reviewPage.getNumber());
        response.put("totalPages", reviewPage.getTotalPages());
        response.put("totalElements", reviewPage.getTotalElements());
        response.put("hasNext", reviewPage.hasNext());
        response.put("hasPrevious", reviewPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 멘토가 받은 후기 목록 조회
    @GetMapping("/mentor/me")
    public ResponseEntity<List<MentoringReviewResponseDto>> getReviewsAsMentor() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MentoringReviewResponseDto> reviews =
                reviewService.getReviewsByMentor(currentUser.getId(), Pageable.unpaged());

        return ResponseEntity.ok(reviews);
    }

    // 현재 로그인한 멘티가 작성한 후기 목록 조회
    @GetMapping("/mentee/me")
    public ResponseEntity<List<MentoringReviewResponseDto>> getReviewsAsMentee() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MentoringReviewResponseDto> reviews =
                reviewService.getReviewsByMentee(currentUser.getId(), Pageable.unpaged());

        return ResponseEntity.ok(reviews);
    }


    // 현재 로그인한 사용자 반환
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByEmail(auth.getName());
    }
}
