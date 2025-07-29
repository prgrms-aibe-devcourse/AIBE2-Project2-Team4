package com.example.portpilot.domain.study.controller;

import com.example.portpilot.domain.study.dto.StudyCreateRequestDto;
import com.example.portpilot.domain.study.dto.StudyDetailResponseDto;
import com.example.portpilot.domain.study.entity.JobType;
import com.example.portpilot.domain.study.entity.StudyRecruitment;
import com.example.portpilot.domain.study.entity.StudyTechStack;
import com.example.portpilot.domain.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies")
public class StudyApiController {

    private final StudyService studyService;

    // 스터디 생성
    @PostMapping
    public ResponseEntity<String> createStudy(@RequestBody StudyCreateRequestDto dto) {
        try {
            studyService.createStudy(dto);
            return ResponseEntity.ok("스터디가 생성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 스터디 탭별 목록 조회
    @GetMapping("/tab-studies")
    public ResponseEntity<List<Map<String, Object>>> getStudiesByTab(@RequestParam String tab) {
        try {
            List<StudyRecruitment> studies = studyService.getStudiesByTab(tab);
            List<Map<String, Object>> result = studies.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    // StudyRecruitment -> Map 변환 (프론트 전달용)
    private Map<String, Object> convertToMap(StudyRecruitment study) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", study.getId());
        map.put("title", study.getTitle());
        map.put("content", study.getContent());
        map.put("backendRecruit", study.getBackendRecruit());
        map.put("frontendRecruit", study.getFrontendRecruit());
        map.put("designerRecruit", study.getDesignerRecruit());
        map.put("plannerRecruit", study.getPlannerRecruit());
        map.put("deadline", study.getDeadline());
        map.put("closed", study.isClosed());
        map.put("completed", study.isCompleted());

        // 작성자 정보
        Map<String, Object> userInfo = new HashMap<>();
        if (study.getUser() != null) {
            userInfo.put("name", study.getUser().getName());
        } else {
            userInfo.put("name", "알 수 없음");
        }
        map.put("user", userInfo);

        // 기술 스택
        List<String> techStacks = study.getTechStacks().stream()
                .map(StudyTechStack::getTechStack)
                .collect(Collectors.toList());
        map.put("techStacks", techStacks);

        return map;
    }

    // 스터디 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<StudyDetailResponseDto> getStudyDetail(@PathVariable Long id) {
        try {
            StudyDetailResponseDto detail = studyService.getStudyDetail(id);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 스터디 신청
    @PostMapping("/{id}/apply")
    public ResponseEntity<String> applyToStudy(@PathVariable Long id,
                                               @RequestBody Map<String, String> request) {
        try {
            JobType jobType = JobType.valueOf(request.get("jobType"));
            studyService.applyToStudy(id, jobType);
            return ResponseEntity.ok("신청이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 신청 수락
    @PostMapping("/applications/{participationId}/accept")
    public ResponseEntity<String> acceptParticipation(@PathVariable Long participationId) {
        try {
            studyService.acceptParticipation(participationId);
            return ResponseEntity.ok("신청이 승인되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 신청 거절
    @PostMapping("/applications/{participationId}/reject")
    public ResponseEntity<String> rejectParticipation(@PathVariable Long participationId) {
        try {
            studyService.rejectParticipation(participationId);
            return ResponseEntity.ok("신청이 거절되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 완료된 스터디 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudy(@PathVariable Long id, Principal principal) {
        try {
            studyService.deleteStudy(id, principal.getName());
            return ResponseEntity.ok("스터디가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 스터디 종료 처리 (테스트)
    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeStudy(@PathVariable Long id) {
        try {
            studyService.completeStudy(id);
            return ResponseEntity.ok("스터디가 종료 처리되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
