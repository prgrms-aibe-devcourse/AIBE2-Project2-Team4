package com.example.portpilot.domain.mentorRequest.Service;

import com.example.portpilot.domain.mentorRequest.dto.MentorProfileDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringRequestCreateDto;
import com.example.portpilot.domain.mentorRequest.dto.MentoringRequestResponseDto;
import com.example.portpilot.domain.mentorRequest.entity.MentorProfile;
import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.mentorRequest.repository.MentorProfileRepository;
import com.example.portpilot.domain.mentorRequest.repository.MentoringRequestRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringService {

    private final MentoringRequestRepository mentoringRequestRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;

    // 멘토 등록
    public void registerMentor(MentorProfileDto dto, User user) {
        if (mentorProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalStateException("이미 멘토로 등록되어 있습니다.");
        }

        MentorProfile mentorProfile = MentorProfile.builder()
                .user(user)
                .techStack(dto.getTechStack())
                .description(dto.getDescription())
                .build();

        mentorProfileRepository.save(mentorProfile);
    }

    // 멘토링 신청
    public void requestMentoring(MentoringRequestCreateDto dto, User applicant) {
        User mentor = userRepository.findByEmail(dto.getMentorEmail());
        if (mentor == null) {
            throw new IllegalArgumentException("해당 멘토를 찾을 수 없습니다.");
        }

        MentoringRequest request = MentoringRequest.builder()
                .user(applicant)
                .userName(applicant.getName())
                .mentor(mentor)
                .mentorName(mentor.getName())
                .topic(dto.getTopic())
                .message(dto.getMessage())
                .status(MentoringStatus.PENDING)
                .build();

        mentoringRequestRepository.save(request);
    }

    // 내가 멘티로 신청한 멘토링 조회
    public List<MentoringRequestResponseDto> getRequestsByUser(User user) {
        return mentoringRequestRepository.findByUser(user).stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 내가 멘토로 받은 멘토링 요청 조회
    public List<MentoringRequestResponseDto> getRequestsByMentor(User mentor) {
        return mentoringRequestRepository.findByMentor(mentor).stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 수락 또는 완료된 멘토링 조회 (멘토 또는 멘티 모두 포함)
    public List<MentoringRequestResponseDto> getCompletedOrAcceptedRequests(User user) {
        return mentoringRequestRepository.findByUserOrMentor(user, user).stream()
                .filter(r -> r.getStatus() == MentoringStatus.ACCEPTED || r.getStatus() == MentoringStatus.COMPLETED)
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ID로 멘토링 요청 조회
    public MentoringRequest findById(Long id) {
        return mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
    }

    // 상태 변경 (거절 처리 등)
    public void updateStatus(Long id, MentoringStatus status, User currentMentor) {
        MentoringRequest request = mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보가 없습니다."));

        if (request.getMentor() == null) {
            throw new AccessDeniedException("아직 수락할 멘토가 지정되지 않았습니다.");
        }

        if (!request.getMentor().getId().equals(currentMentor.getId())) {
            throw new AccessDeniedException("해당 멘토링 요청에 대한 권한이 없습니다.");
        }

        request.setStatus(status);
        mentoringRequestRepository.save(request);
    }

    // 멘토링 수락
    public void acceptMentoringOnly(Long id, User mentor) {
        MentoringRequest req = mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("멘토링 없음"));

        if (!req.getStatus().equals(MentoringStatus.PENDING)) {
            throw new IllegalStateException("이미 처리됨");
        }

        req.setStatus(MentoringStatus.ACCEPTED);
        req.setMentor(mentor);
        req.setMentorName(mentor.getName());

        mentoringRequestRepository.save(req);
    }

    // 멘토링 방 링크 생성
    private String generateSessionLink(MentoringRequest req) {
        return "https://meet.jit.si/mentoring-" + req.getId();
    }

    // 일정 확정 및 세션 링크 저장
    public void scheduleMentoring(Long id, LocalDateTime scheduledAt) {
        MentoringRequest req = mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("멘토링 없음"));

        if (!req.getStatus().equals(MentoringStatus.ACCEPTED)) {
            throw new IllegalStateException("수락되지 않음");
        }

        req.setScheduledAt(scheduledAt);
        req.setSessionUrl(generateSessionLink(req));
        mentoringRequestRepository.save(req);
    }

    // 일정 지난 멘토링 자동 완료 처리
    @Scheduled(fixedRate = 60000)
    public void completeExpiredMentoring() {
        List<MentoringRequest> list = mentoringRequestRepository.findAllByStatus(MentoringStatus.ACCEPTED);
        for (MentoringRequest req : list) {
            if (req.getScheduledAt() != null &&
                    req.getScheduledAt().isBefore(LocalDateTime.now().minusHours(1))) {
                req.setStatus(MentoringStatus.COMPLETED);
                req.setCompleted(true);
                mentoringRequestRepository.save(req);
            }
        }
    }
}
