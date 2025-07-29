package com.example.portpilot.domain.resume.controller;

import com.example.portpilot.domain.resume.dto.*;
import com.example.portpilot.domain.resume.service.ResumeService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeApiController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getResumeList(Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            List<ResumeResponse> resumes = resumeService.getResumeList(userId);
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            log.error("이력서 목록 조회 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 상세 조회
    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> getResume(
            @PathVariable Long resumeId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            ResumeResponse resume = resumeService.getResume(resumeId, userId);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            log.error("이력서 조회 실패 - resumeId: {}", resumeId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // 기본정보 생성
    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(Authentication authentication,
                                                       @RequestBody @Valid ResumeRequest request) {
        try {
            Long userId = getCurrentUserId(authentication);
            ResumeResponse response = resumeService.createResume(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이력서 생성 실패", e);
            return ResponseEntity.status(500).build();
        }
    }

    // 기본정보 수정
    @PutMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> updateResume(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody ResumeRequest request) {
        try {
            Long userId = getCurrentUserId(authentication);
            ResumeResponse resume = resumeService.updateResume(resumeId, userId, request);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            log.error("이력서 수정 실패 - resumeId: {}", resumeId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // 삭제
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long resumeId,
            Authentication authentication) {
        try {
            Long userId = getCurrentUserId(authentication);
            resumeService.deleteResume(resumeId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("이력서 삭제 실패 - resumeId: {}", resumeId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // 자소서 섹션 저장
    @PostMapping("/{resumeId}/sections")
    public ResponseEntity<Void> saveSection(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody SectionRequest request) {
        try {
            Long userId = getCurrentUserId(authentication);
            resumeService.saveSection(resumeId, userId, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("섹션 저장 실패 - resumeId: {}", resumeId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // 학력 추가
    @PostMapping("/{resumeId}/educations")
    public ResponseEntity<Void> addEducation(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody EducationRequest request) {
        try {
            log.info("학력 추가 요청 - resumeId: {}, request: {}", resumeId, request);
            Long userId = getCurrentUserId(authentication);
            log.info("사용자 ID: {}", userId);
            resumeService.addEducation(resumeId, userId, request);
            log.info("학력 추가 성공");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("학력 추가 실패 - resumeId: {}, request: {}", resumeId, request, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // 경력 추가
    @PostMapping("/{resumeId}/careers")
    public ResponseEntity<Void> addCareer(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody CareerRequest request) {
        try {
            log.info("경력 추가 요청 - resumeId: {}, request: {}", resumeId, request);
            Long userId = getCurrentUserId(authentication);
            resumeService.addCareer(resumeId, userId, request);
            log.info("경력 추가 성공");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("경력 추가 실패 - resumeId: {}, request: {}", resumeId, request, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // 경험/활동 추가
    @PostMapping("/{resumeId}/experiences")
    public ResponseEntity<Void> addExperience(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody ExperienceRequest request) {
        try {
            log.info("경험 추가 요청 - resumeId: {}, request: {}", resumeId, request);
            Long userId = getCurrentUserId(authentication);
            resumeService.addExperience(resumeId, userId, request);
            log.info("경험 추가 성공");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("경험 추가 실패 - resumeId: {}, request: {}", resumeId, request, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // 파일 내보내기
    @PostMapping("/{resumeId}/export")
    public ResponseEntity<Resource> exportResume(
            @PathVariable Long resumeId,
            Authentication authentication,
            @RequestBody ExportRequest request) {

        Long userId = getCurrentUserId(authentication);

        try {
            // 파일 생성 및 Resource 반환
            Resource resource = resumeService.exportResume(resumeId, userId, request.getFormat());

            // 파일명 설정
            ResumeResponse resume = resumeService.getResume(resumeId, userId);
            String filename = generateFilename(resume.getTitle(), request.getFormat());

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", getContentType(request.getFormat()))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String generateFilename(String title, String format) {
        String safeTitle = title != null ? title.replaceAll("[^가-힣a-zA-Z0-9\\s]", "") : "자기소개서";
        String extension = format.equals("docx") ? "docx" : format;
        return safeTitle + "." + extension;
    }

    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt":
            default:
                return "text/plain;charset=UTF-8";
        }
    }

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