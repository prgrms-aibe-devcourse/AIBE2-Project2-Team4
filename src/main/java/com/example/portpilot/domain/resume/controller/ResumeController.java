package com.example.portpilot.domain.resume.controller;

import com.example.portpilot.domain.resume.dto.*;
import com.example.portpilot.domain.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getResumeList(
            @RequestHeader("X-User-Id") Long userId) {
        List<ResumeResponse> resumes = resumeService.getResumeList(userId);
        return ResponseEntity.ok(resumes);
    }

    // 상세 조회
    @GetMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> getResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {
        ResumeResponse resume = resumeService.getResume(resumeId, userId);
        return ResponseEntity.ok(resume);
    }

    // 기본정보 생성
    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(@RequestHeader("X-User-Id") Long userId,
                                                       @RequestBody @Valid ResumeRequest request) {
        ResumeResponse response = resumeService.createResume(userId, request);

        return ResponseEntity.ok(response);
    }

    // 기본정보 수정
    @PutMapping("/{resumeId}")
    public ResponseEntity<ResumeResponse> updateResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ResumeRequest request) {
        ResumeResponse resume = resumeService.updateResume(resumeId, userId, request);
        return ResponseEntity.ok(resume);
    }

    // 삭제
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {
        resumeService.deleteResume(resumeId, userId);
        return ResponseEntity.ok().build();
    }

    // 자소서 섹션 저장
    @PostMapping("/{resumeId}/sections")
    public ResponseEntity<Void> saveSection(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SectionRequest request) {
        resumeService.saveSection(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 학력 추가
    @PostMapping("/{resumeId}/educations")
    public ResponseEntity<Void> addEducation(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody EducationRequest request) {
        resumeService.addEducation(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 경력 추가
    @PostMapping("/{resumeId}/careers")
    public ResponseEntity<Void> addCareer(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CareerRequest request) {
        resumeService.addCareer(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }

    // 경험/활동 추가
    @PostMapping("/{resumeId}/experiences")
    public ResponseEntity<Void> addExperience(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ExperienceRequest request) {
        resumeService.addExperience(resumeId, userId, request);
        return ResponseEntity.ok().build();
    }
}
