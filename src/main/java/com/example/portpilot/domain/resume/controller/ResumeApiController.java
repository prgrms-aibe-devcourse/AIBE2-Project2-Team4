package com.example.portpilot.domain.resume.controller;

import com.example.portpilot.domain.resume.dto.*;
import com.example.portpilot.domain.resume.service.ResumeService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeApiController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getResumeList(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ResumeResponse> resumes = resumeService.getResumeList(userId);
        return ResponseEntity.ok(resumes);
    }

    // 상세 조회
    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> getResume(
            @PathVariable Long resumeId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        ResumeResponse resume = resumeService.getResume(resumeId, userId);
        return ResponseEntity.ok(resume);
    }

    // 기본정보 생성
    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(Authentication authentication,
                                                       @RequestBody @Valid ResumeRequest request) {
        Long userId = getCurrentUserId(authentication);
        ResumeResponse response = resumeService.createResume(userId, request);

        return ResponseEntity.ok(response);
    }

    // 기본정보 수정
    @PutMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> updateResume(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody ResumeRequest request) {
        Long userId = getCurrentUserId(authentication);
        ResumeResponse resume = resumeService.updateResume(resumeId, userId, request);
        return ResponseEntity.ok(resume);
    }

    // 삭제
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long resumeId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        resumeService.deleteResume(resumeId, userId);
        return ResponseEntity.ok().build();
    }

    // 자소서 섹션 저장
    @PostMapping("/{resumeId}/sections")
    public ResponseEntity<Void> saveSection(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody SectionRequest request) {
        Long userId = getCurrentUserId(authentication);
        resumeService.saveSection(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 학력 추가
    @PostMapping("/{resumeId}/educations")
    public ResponseEntity<Void> addEducation(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody EducationRequest request) {
        Long userId = getCurrentUserId(authentication);
        resumeService.addEducation(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 경력 추가
    @PostMapping("/{resumeId}/careers")
    public ResponseEntity<Void> addCareer(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody CareerRequest request) {
        Long userId = getCurrentUserId(authentication);
        resumeService.addCareer(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 경험/활동 추가
    @PostMapping("/{resumeId}/experiences")
    public ResponseEntity<Void> addExperience(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody ExperienceRequest request) {
        Long userId = getCurrentUserId(authentication);
        resumeService.addExperience(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        return user.getId();
    }
}
