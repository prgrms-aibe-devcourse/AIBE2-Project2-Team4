package com.example.portpilot.domain.resume.controller;

import com.example.portpilot.domain.resume.dto.ResumeResponse;
import com.example.portpilot.domain.resume.service.GeminiService;
import com.example.portpilot.domain.resume.service.ResumeService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final ResumeService resumeService;
    private final UserRepository userRepository;

    @GetMapping("/chat")
    public ResponseEntity<?> gemini() {
        try {
            return ResponseEntity.ok().body(geminiService.getContents("안녕! 너는 누구야?"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이력서 기반 자소서 생성
    @PostMapping("/generate/{resumeId}")
    public ResponseEntity<?> generateCoverLetter(@PathVariable Long resumeId,
                                                 Authentication authentication) {
        try {
            log.info("자소서 생성 요청 - resumeId: {}", resumeId);
            Long userId = getCurrentUserId(authentication);
            log.info("사용자 ID: {}", userId);
            geminiService.generateCoverLetterSections(userId, resumeId);
            log.info("자소서 생성 완료");
            return ResponseEntity.ok().body("자소서 생성이 완료되었습니다.");
        } catch (Exception e) {
            log.error("자소서 생성 실패 - resumeId: {}", resumeId, e);
            return ResponseEntity.badRequest().body("자소서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 생성된 자소서 조회
    @GetMapping("/result/{resumeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCoverLetterResult(@PathVariable Long resumeId,
                                                  Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("인증되지 않은 사용자의 자소서 조회 시도");
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            Long userId;
            try {
                userId = getCurrentUserId(authentication);
                log.info("사용자 인증 성공 - userId: {}", userId);
            } catch (Exception e) {
                log.error("사용자 인증 실패", e);
                return ResponseEntity.status(401).body("사용자 인증에 실패했습니다: " + e.getMessage());
            }

            ResumeResponse resume = resumeService.getResume(resumeId, userId);
            return ResponseEntity.ok().body(resume);
        } catch (Exception e) {
            log.error("자소서 조회 실패 - resumeId: {}", resumeId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 현재 로그인한 사용자 ID 추출
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("인증되지 않은 사용자 접근");
            throw new RuntimeException("로그인이 필요합니다.");
        }

        String email = authentication.getName();
        log.info("사용자 이메일: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("사용자를 찾을 수 없음: {}", email);
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + email);
        }

        log.info("사용자 ID: {}", user.getId());
        return user.getId();
    }
}