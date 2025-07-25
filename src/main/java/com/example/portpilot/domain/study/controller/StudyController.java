package com.example.portpilot.domain.study.controller;

import com.example.portpilot.domain.study.dto.StudyApplicantDto;
import com.example.portpilot.domain.study.dto.StudyCreateRequestDto;
import com.example.portpilot.domain.study.entity.StudyApplyStatus;
import com.example.portpilot.domain.study.entity.StudyParticipation;
import com.example.portpilot.domain.study.entity.StudyRecruitment;
import com.example.portpilot.domain.study.service.StudyService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final UserRepository userRepository;

    // 현재 사용자 가져오기
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    // 스터디 목록 페이지
    @GetMapping
    public String getStudyList() {
        return "study/studyList";
    }

    // 스터디 생성 폼
    @GetMapping("/create")
    public String showCreateForm() {
        return "study/studyCreate";
    }

    // 스터디 생성 처리
    @PostMapping("/create")
    public String createStudy(@ModelAttribute StudyCreateRequestDto dto) {
        try {
            studyService.createStudy(dto);
            return "redirect:/study";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/study/create?error=" + e.getMessage();
        }
    }

    // 스터디 상세 페이지
    @GetMapping("/{id}")
    public String getStudyDetail(@PathVariable Long id, Model model) {
        try {
            StudyRecruitment study = studyService.getStudyForDetail(id);
            User currentUser = getCurrentUser();

            // 참여자 목록 (승인된 사람들)
            List<StudyParticipation> acceptedList = studyService.getParticipants(study, StudyApplyStatus.ACCEPTED);

            // 신청 대기 목록 (작성자만)
            List<StudyParticipation> pendingList = studyService.getParticipants(study, StudyApplyStatus.PENDING);

            // 작성자 여부 확인
            boolean isOwner = false;
            if (currentUser != null && study.getUser() != null) {
                isOwner = study.getUser().getId().equals(currentUser.getId());
            }

            // 참여자 여부 확인 (승인된 상태)
            boolean isParticipant = false;
            if (currentUser != null) {
                isParticipant = acceptedList.stream()
                        .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
            }

            model.addAttribute("study", study);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isParticipant", isParticipant);

            boolean hasPendingApplication = false;
            if (currentUser != null) {
                hasPendingApplication = pendingList.stream()
                        .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));
            }
            model.addAttribute("hasPendingApplication", hasPendingApplication);

            // 참여자 목록 변환
            List<StudyApplicantDto> participants = acceptedList.stream()
                    .map(p -> StudyApplicantDto.builder()
                            .participationId(p.getId())
                            .userId(p.getUser().getId())
                            .name(p.getUser().getName())
                            .email(p.getUser().getEmail())
                            .jobType(p.getJobType().name())
                            .build())
                    .collect(Collectors.toList());

            ObjectMapper objectMapper = new ObjectMapper();
            String participantsJson = objectMapper.writeValueAsString(participants);
            model.addAttribute("participantsJson", participantsJson);
            model.addAttribute("participants", participants);

            // 대기 목록 (작성자만)
            if (isOwner) {
                List<StudyApplicantDto> pendingApplicants = pendingList.stream()
                        .map(p -> StudyApplicantDto.builder()
                                .participationId(p.getId())
                                .userId(p.getUser().getId())
                                .name(p.getUser().getName())
                                .email(p.getUser().getEmail())
                                .jobType(p.getJobType().name())
                                .build())
                        .collect(Collectors.toList());

                model.addAttribute("pendingList", pendingApplicants);
            }

            return "study/studyDetail";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/study?error=" + e.getMessage();
        }
    }
}