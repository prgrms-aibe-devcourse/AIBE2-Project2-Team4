package com.example.portpilot.domain.study.controller;

import com.example.portpilot.domain.study.entity.StudyApplyStatus;
import com.example.portpilot.domain.study.dto.StudyApplicantDto;
import com.example.portpilot.domain.study.dto.StudyCreateRequestDto;
import com.example.portpilot.domain.study.dto.StudyDetailResponseDto;
import com.example.portpilot.domain.study.entity.StudyParticipation;
import com.example.portpilot.domain.study.StudyRecruitment;
import com.example.portpilot.domain.study.repository.StudyParticipationRepository;
import com.example.portpilot.domain.study.repository.StudyRecruitmentRepository;
import com.example.portpilot.domain.study.service.StudyService;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyRecruitmentRepository studyRepo;
    private final StudyParticipationRepository participationRepo;
    private final StudyService studyService;

    // 스터디 목록 페이지
    @GetMapping
    public String getStudyList(Model model) {
        List<StudyRecruitment> studyList = studyRepo.findAll();
        model.addAttribute("studyList", studyList);
        return "study/studyList";
    }

    // 스터디 생성 폼 보여주기
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        return "study/studyCreate";
    }

    // 스터디 생성 처리
    @PostMapping("/create")
    public String createStudy(@ModelAttribute StudyCreateRequestDto dto,
                              @AuthenticationPrincipal User user) {
        studyService.createStudy(dto, user);
        return "redirect:/study"; // 목록으로 이동
    }

    // 스터디 상세 페이지
    @GetMapping("/{id}")
    public String getStudyDetail(@PathVariable Long id,
                                 @AuthenticationPrincipal User user,
                                 Model model) {

        StudyRecruitment study = studyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        // 참여자 목록 (수락된 사람만)
        List<StudyParticipation> acceptedList = participationRepo.findByStudyAndStatus(study, StudyApplyStatus.ACCEPTED);

        // 신청 대기 목록 (작성자만 볼 수 있음)
        List<StudyParticipation> pendingList = participationRepo.findByStudyAndStatus(study, StudyApplyStatus.PENDING);

        // 현재 로그인 유저가 작성자인지 여부
        boolean isOwner = user != null && study.getUser().getId().equals(user.getId());

        model.addAttribute("study", study);
        model.addAttribute("isOwner", isOwner);

        model.addAttribute("participants", acceptedList.stream()
                .map(p -> StudyApplicantDto.builder()
                        .participationId(p.getId())
                        .userId(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .jobType(p.getJobType())
                        .build()).collect(Collectors.toList()));

        if (isOwner) {
            model.addAttribute("pendingList", pendingList.stream()
                    .map(p -> StudyApplicantDto.builder()
                            .participationId(p.getId())
                            .userId(p.getUser().getId())
                            .name(p.getUser().getName())
                            .email(p.getUser().getEmail())
                            .jobType(p.getJobType())
                            .build()).collect(Collectors.toList()));
        }

        return "study/studyDetail";
    }
}
