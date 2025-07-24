package com.example.portpilot.domain.study.service;

import com.example.portpilot.domain.study.entity.StudyApplyStatus;
import com.example.portpilot.domain.study.dto.*;
import com.example.portpilot.domain.study.entity.StudyParticipation;
import com.example.portpilot.domain.study.StudyRecruitment;
import com.example.portpilot.domain.study.entity.JobType;
import com.example.portpilot.domain.study.entity.StudyTechStack;
import com.example.portpilot.domain.study.repository.StudyParticipationRepository;
import com.example.portpilot.domain.study.repository.StudyRecruitmentRepository;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRecruitmentRepository studyRepo;
    private final StudyParticipationRepository participationRepo;

    // 스터디 생성
    public void createStudy(StudyCreateRequestDto dto, User user) {
        // 기본 정보로 StudyRecruitment 엔티티 생성
        StudyRecruitment study = StudyRecruitment.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .deadline(dto.getDeadline())
                .backendRecruit(dto.getBackendRecruit())
                .frontendRecruit(dto.getFrontendRecruit())
                .designerRecruit(dto.getDesignerRecruit())
                .plannerRecruit(dto.getPlannerRecruit())
                .user(user)
                .build();

        studyRepo.save(study);

        // 직종별 기술스택 설정
        List<StudyTechStack> techStackList = new ArrayList<>();
        addStacks(techStackList, dto.getTechStacks_BACKEND(), JobType.BACKEND, study);
        addStacks(techStackList, dto.getTechStacks_FRONTEND(), JobType.FRONTEND, study);
        addStacks(techStackList, dto.getTechStacks_DESIGNER(), JobType.DESIGNER, study);
        addStacks(techStackList, dto.getTechStacks_PLANNER(), JobType.PLANNER, study);
        study.setTechStacks(techStackList);

        // 다시 저잗
        studyRepo.save(study);
    }

    // 직종별 기술스택 리스트 생성 메서드
    private void addStacks(List<StudyTechStack> stackList, List<String> techStacks, JobType jobType, StudyRecruitment study) {
        if (techStacks == null || techStacks.isEmpty()) return;

        for (String stack : techStacks) {
            StudyTechStack tech = StudyTechStack.builder()
                    .study(study)
                    .jobType(jobType)
                    .techStack(stack)
                    .build();
            stackList.add(tech);
        }
    }

    // 스터디 참가 신청
    public void applyToStudy(StudyParticipationRequestDto dto, User user) {
        StudyRecruitment study = studyRepo.findById(dto.getStudyId())
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        // 마감 여부 확인
        if (study.isRecruitmentClosed()) {
            throw new IllegalStateException("모집이 마감된 스터디입니다.");
        }

        // 중복 신청 확인
        participationRepo.findByStudyAndUser(study, user).ifPresent(p -> {
            throw new IllegalStateException("이미 신청한 스터디입니다.");
        });

        // 직종별 정원 초과 여부 확인
        long count = participationRepo.findByStudyAndStatus(study, StudyApplyStatus.ACCEPTED).stream()
                .filter(p -> p.getJobType() == dto.getJobType())
                .count();

        int limit = getLimitByJobType(study, dto.getJobType());
        if (count >= limit) {
            throw new IllegalStateException("해당 직군은 정원이 마감되었습니다.");
        }

        // 신청 엔티티 생성 및 저장
        StudyParticipation participation = StudyParticipation.builder()
                .study(study)
                .user(user)
                .jobType(dto.getJobType())
                .status(StudyApplyStatus.PENDING)
                .build();

        participationRepo.save(participation);
    }

    // 참가 신청 수락
    public void acceptParticipation(Long studyId, Long participationId, User user) {
        StudyRecruitment study = getOwnedStudy(studyId, user);
        StudyParticipation participation = participationRepo.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("신청이 존재하지 않습니다."));

        if (participation.getStatus() != StudyApplyStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신청입니다.");
        }

        participation.setStatus(StudyApplyStatus.ACCEPTED);
    }

    // 참가 신청 거절
    public void rejectParticipation(Long studyId, Long participationId, User user) {
        StudyRecruitment study = getOwnedStudy(studyId, user);
        StudyParticipation participation = participationRepo.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("신청이 존재하지 않습니다."));

        participation.setStatus(StudyApplyStatus.REJECTED);
    }

    // 스터디 모집 마감
    public void closeStudy(Long studyId, User user) {
        StudyRecruitment study = getOwnedStudy(studyId, user);
        study.setClosed(true);
    }

    // 스터디 상세 정보 조회
    public StudyDetailResponseDto getStudyDetail(Long id) {
        StudyRecruitment study = studyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        // 참여자 목록 변환
        List<StudyParticipantDto> participants = participationRepo.findByStudyAndStatus(study, StudyApplyStatus.ACCEPTED)
                .stream()
                .map(p -> StudyParticipantDto.builder()
                        .userId(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .jobType(p.getJobType())
                        .build())
                .collect(Collectors.toList());

        // 응답 DTO 반환
        return StudyDetailResponseDto.builder()
                .id(study.getId())
                .title(study.getTitle())
                .content(study.getContent())
                .techStack(study.getTechStack())
                .maxMembers(study.getMaxMembers())
                .backendRecruit(study.getBackendRecruit())
                .frontendRecruit(study.getFrontendRecruit())
                .designerRecruit(study.getDesignerRecruit())
                .plannerRecruit(study.getPlannerRecruit())
                .deadline(study.getDeadline())
                .isClosed(study.isClosed())
                .isBlocked(study.isBlocked())
                .participants(participants)
                .build();
    }

    // 작성자 본인 여부 확인 후 Study 엔티티 반환
    private StudyRecruitment getOwnedStudy(Long studyId, User user) {
        StudyRecruitment study = studyRepo.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));
        if (!study.getUser().getId().equals(user.getId())) {
            throw new SecurityException("스터디 작성자만 수행할 수 있는 작업입니다.");
        }
        return study;
    }

    // 직종별 모집 인원 제한 수 반환
    private int getLimitByJobType(StudyRecruitment study, JobType jobType) {
        switch (jobType) {
            case BACKEND:
                return study.getBackendRecruit();
            case FRONTEND:
                return study.getFrontendRecruit();
            case DESIGNER:
                return study.getDesignerRecruit();
            case PLANNER:
                return study.getPlannerRecruit();
            default:
                throw new IllegalArgumentException("존재하지 않는 직종입니다.");
        }
    }

}
