package com.example.portpilot.domain.study.controller;

import com.example.portpilot.domain.study.dto.*;
import com.example.portpilot.domain.study.service.StudyService;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies")
public class StudyApiController {

    private final StudyService studyService;

    // 스터디 등록
    @PostMapping
    public void createStudy(@RequestBody StudyCreateRequestDto dto,
                            @AuthenticationPrincipal User user) {
        studyService.createStudy(dto, user);
    }

    // 스터디 상세 조회
    @GetMapping("/{id}")
    public StudyDetailResponseDto getStudyDetail(@PathVariable Long id) {
        return studyService.getStudyDetail(id);
    }

    // 스터디 신청
    @PostMapping("/{id}/apply")
    public void applyToStudy(@PathVariable Long id,
                             @RequestBody StudyParticipationRequestDto dto,
                             @AuthenticationPrincipal User user) {
        dto.setStudyId(id); // PathVariable → DTO에 주입
        studyService.applyToStudy(dto, user);
    }

    // 신청 수락
    @PostMapping("/{studyId}/applications/{participationId}/accept")
    public void acceptParticipation(@PathVariable Long studyId,
                                    @PathVariable Long participationId,
                                    @AuthenticationPrincipal User user) {
        studyService.acceptParticipation(studyId, participationId, user);
    }

    // 신청 거절
    @PostMapping("/{studyId}/applications/{participationId}/reject")
    public void rejectParticipation(@PathVariable Long studyId,
                                    @PathVariable Long participationId,
                                    @AuthenticationPrincipal User user) {
        studyService.rejectParticipation(studyId, participationId, user);
    }

    // 스터디 수동 마감
    @PostMapping("/{id}/close")
    public void closeStudy(@PathVariable Long id,
                           @AuthenticationPrincipal User user) {
        studyService.closeStudy(id, user);
    }
}
