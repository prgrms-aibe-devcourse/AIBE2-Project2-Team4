package com.example.portpilot.domain.study.service;

import com.example.portpilot.domain.study.entity.*;
import com.example.portpilot.domain.study.dto.*;
import com.example.portpilot.domain.study.repository.StudyParticipationRepository;
import com.example.portpilot.domain.study.repository.StudyRecruitmentRepository;
import com.example.portpilot.domain.study.repository.StudyRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRecruitmentRepository studyRepo;
    private final StudyParticipationRepository participationRepo;
    private final UserRepository userRepository;
    private final StudyRecruitmentRepository studyRecruitmentRepo;
    private final StudyRepository studyRepository;

    // 현재 로그인된 사용자 가져오기
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalStateException("등록되지 않은 사용자입니다.");
        }

        return user;
    }

    // 스터디 생성
    public void createStudy(StudyCreateRequestDto dto) {
        User currentUser = getCurrentUser();

        StudyRecruitment study = StudyRecruitment.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .deadline(dto.getDeadline().atTime(23, 59, 59))
                .backendRecruit(dto.getBackendRecruit())
                .frontendRecruit(dto.getFrontendRecruit())
                .designerRecruit(dto.getDesignerRecruit())
                .plannerRecruit(dto.getPlannerRecruit())
                .user(currentUser)
                .closed(false)
                .build();

        // 기술스택 저장
        study.addTechStacks(JobType.BACKEND, dto.getTechStacks_BACKEND());
        study.addTechStacks(JobType.FRONTEND, dto.getTechStacks_FRONTEND());
        study.addTechStacks(JobType.DESIGNER, dto.getTechStacks_DESIGNER());
        study.addTechStacks(JobType.PLANNER, dto.getTechStacks_PLANNER());

        studyRepo.save(study);
    }

    // 스터디 목록 조회 (탭별)
    @Transactional(readOnly = true)
    public List<StudyRecruitment> getStudiesByTab(String tab) {
        User currentUser = getCurrentUser();

        switch (tab) {
            case "recruiting":
                return studyRepo.findByClosedFalseAndCompletedFalse();
            case "closed":
                return studyRepo.findByClosedTrueAndCompletedFalse();
            case "completed":
                return studyRepo.findByCompletedTrue();
            case "my":
                return currentUser != null ? studyRepo.findByUser(currentUser) : List.of();
            case "applied":
                return getAppliedStudies(currentUser);
            default:
                return studyRepo.findByClosedFalseAndCompletedFalse();
        }
    }

    private List<StudyRecruitment> getAppliedStudies(User user) {
        if (user == null) return List.of();
        return participationRepo.findByUser(user).stream()
                .map(StudyParticipation::getStudy)
                .distinct()
                .collect(Collectors.toList());
    }

    // 스터디 상세 조회
    @Transactional(readOnly = true)
    public StudyDetailResponseDto getStudyDetail(Long id) {
        StudyRecruitment study = studyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        // 차단된 스터디 접근 차단
        if (study.isBlocked()) {
            throw new IllegalArgumentException("삭제된 스터디입니다.");
        }

        List<StudyParticipantDto> participants = participationRepo.findByStudyAndStatus(study, StudyApplyStatus.ACCEPTED)
                .stream()
                .map(p -> StudyParticipantDto.builder()
                        .userId(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .jobType(p.getJobType())
                        .build())
                .collect(Collectors.toList());

        return StudyDetailResponseDto.builder()
                .id(study.getId())
                .title(study.getTitle())
                .content(study.getContent())
                .techStack(getTechStackString(study))
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

    private String getTechStackString(StudyRecruitment study) {
        return study.getTechStacks().stream()
                .map(StudyTechStack::getTechStack)
                .collect(Collectors.joining(", "));
    }

    // 스터디 신청
    public void applyToStudy(Long studyId, JobType jobType) {
        User currentUser = getCurrentUser();
        StudyRecruitment study = studyRepo.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        if (study.isRecruitmentClosed()) {
            throw new IllegalStateException("모집이 마감된 스터디입니다.");
        }

        if (participationRepo.findByStudyAndUser(study, currentUser).isPresent()) {
            throw new IllegalStateException("이미 신청한 스터디입니다.");
        }

        long currentCount = participationRepo.findByStudyAndStatus(study, StudyApplyStatus.ACCEPTED)
                .stream()
                .filter(p -> p.getJobType() == jobType)
                .count();

        int limit = getJobLimit(study, jobType);
        if (currentCount >= limit) {
            throw new IllegalStateException("해당 직군은 정원이 마감되었습니다.");
        }

        StudyParticipation participation = StudyParticipation.builder()
                .study(study)
                .user(currentUser)
                .jobType(jobType)
                .status(StudyApplyStatus.PENDING)
                .build();

        participationRepo.save(participation);
    }

    private int getJobLimit(StudyRecruitment study, JobType jobType) {
        switch (jobType) {
            case BACKEND: return study.getBackendRecruit();
            case FRONTEND: return study.getFrontendRecruit();
            case DESIGNER: return study.getDesignerRecruit();
            case PLANNER: return study.getPlannerRecruit();
            default: throw new IllegalArgumentException("잘못된 직종입니다.");
        }
    }

    // 신청 수락
    public void acceptParticipation(Long participationId) {
        StudyParticipation participation = participationRepo.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("신청이 존재하지 않습니다."));

        User currentUser = getCurrentUser();
        if (!participation.getStudy().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("스터디 작성자만 승인할 수 있습니다.");
        }

        participation.setStatus(StudyApplyStatus.ACCEPTED);
    }

    // 신청 거절
    public void rejectParticipation(Long participationId) {
        StudyParticipation participation = participationRepo.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("신청이 존재하지 않습니다."));

        User currentUser = getCurrentUser();
        if (!participation.getStudy().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("스터디 작성자만 거절할 수 있습니다.");
        }

        participation.setStatus(StudyApplyStatus.REJECTED);
    }

    // 스터디 마감 처리
    public void closeStudy(Long studyId) {
        StudyRecruitment study = studyRepo.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        User currentUser = getCurrentUser();
        if (!study.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("스터디 작성자만 마감할 수 있습니다.");
        }

        study.setClosed(true);
    }

    // 스터디 상세 조회
    @Transactional(readOnly = true)
    public StudyRecruitment getStudyForDetail(Long id) {
        StudyRecruitment study = studyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        // 차단된 스터디 접근 차단
        if (study.isBlocked()) {
            throw new IllegalArgumentException("삭제된 스터디입니다.");
        }

        return study;
    }

    // 참여자 목록 조회
    @Transactional(readOnly = true)
    public List<StudyParticipation> getParticipants(StudyRecruitment study, StudyApplyStatus status) {
        return participationRepo.findByStudyAndStatus(study, status);
    }

    // 스터디 삭제
    @Transactional
    public void deleteStudy(Long id, String userEmail) throws IllegalAccessException {
        StudyRecruitment study = studyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));

        System.out.println("요청자 email: " + userEmail);
        System.out.println("작성자 email: " + study.getUser().getEmail());
        System.out.println("스터디 완료 여부: " + study.isCompleted());

        if (!study.getUser().getEmail().equals(userEmail)) {
            throw new IllegalAccessException("삭제 권한이 없습니다.");
        }

        if (!study.isCompleted()) {
            throw new IllegalStateException("아직 종료되지 않은 스터디입니다.");
        }

        studyRepository.delete(study);
    }

    // 스터디 강제 완료 처리 테스트용이에요
    public void completeStudy(Long studyId) {
        StudyRecruitment study = studyRepo.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("스터디가 존재하지 않습니다."));
        study.setCompleted(true);
    }

    //메인에 띄우기용
    @Transactional(readOnly = true)
    public List<StudyRecruitment> findAllOpenStudies() {
        return studyRepository.findByClosedFalseAndCompletedFalse();
    }

    //메인에 띄우는 갯수 제한
    public List<StudyRecruitment> findLatestOpenStudies(int limit) {
        return studyRepository.findByClosedFalseAndIsBlockedFalse()
                .stream()
                .sorted(Comparator.comparing(StudyRecruitment::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

}
