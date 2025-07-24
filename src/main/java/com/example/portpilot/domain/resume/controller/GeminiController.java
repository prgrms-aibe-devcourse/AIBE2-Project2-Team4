package com.example.portpilot.domain.resume.controller;

import com.example.portpilot.domain.resume.dto.ResumeResponse;
import com.example.portpilot.domain.resume.service.GeminiService;
import com.example.portpilot.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gemini")
public class GeminiController {

    private final GeminiService geminiService;
    private final ResumeService resumeService;

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
                                                 @RequestHeader("X-User-Id") Long userId) {
        try {
            geminiService.generateCoverLetterSections(userId, resumeId);
            return ResponseEntity.ok().body("자소서 생성이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("자소서 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 생성된 자소서 조회
    @GetMapping("/result/{resumeId}")
    public ResponseEntity<?> getCoverLetterResult(@PathVariable Long resumeId,
                                                  @RequestHeader("X-User-Id") Long userId) {
        try {
            ResumeResponse resume = resumeService.getResume(userId, resumeId);
            return ResponseEntity.ok().body(resume);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}