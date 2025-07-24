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
import java.util.*;
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
        if (mentorProfileRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("이미 멘토로 등록되어 있습니다.");
        }
        MentorProfile profile = new MentorProfile();
        profile.setUser(user);
        profile.setTechStack(dto.getTechStack());
        profile.setDescription(dto.getDescription());
        mentorProfileRepository.save(profile);
    }

    // 멘토링 신청
    public void requestMentoring(MentoringRequestCreateDto dto, User user) {
        User mentor = userRepository.findByEmail(dto.getMentorEmail());
        if (mentor == null) throw new IllegalArgumentException("해당 멘토를 찾을 수 없습니다.");

        MentoringRequest request = new MentoringRequest();
        request.setUser(user);
        request.setUserName(user.getName());
        request.setMentor(mentor);
        request.setMentorName(mentor.getName());
        request.setTopic(dto.getTopic());
        request.setMessage(dto.getMessage());
        request.setStatus(MentoringStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        mentoringRequestRepository.save(request);
    }

    // 멘티로 신청한 요청 목록
    public List<MentoringRequestResponseDto> getRequestsByUser(User user) {
        return mentoringRequestRepository.findByUser(user).stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 멘토로 받은 요청 목록
    public List<MentoringRequestResponseDto> getRequestsByMentor(User mentor) {
        return mentoringRequestRepository.findByMentor(mentor).stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 수락 혹은 완료된 멘토링 요청
    public List<MentoringRequestResponseDto> getCompletedOrAcceptedRequests(User user) {
        return mentoringRequestRepository.findByUserOrMentor(user, user).stream()
                .filter(req -> req.getStatus() == MentoringStatus.ACCEPTED || req.getStatus() == MentoringStatus.COMPLETED)
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public MentoringRequest findById(Long id) {
        return mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 존재하지 않습니다."));
    }

    public void acceptMentoringOnly(Long id, User mentor) {
        MentoringRequest req = findById(id);
        if (!req.getStatus().equals(MentoringStatus.PENDING)) {
            throw new IllegalStateException("이미 처리됨");
        }
        req.setStatus(MentoringStatus.ACCEPTED);
        req.setMentor(mentor);
        req.setMentorName(mentor.getName());
        mentoringRequestRepository.save(req);
    }

    public void updateStatus(Long id, MentoringStatus status, User currentUser) {
        MentoringRequest req = findById(id);
        if (!Objects.equals(req.getMentor(), currentUser) && !Objects.equals(req.getUser(), currentUser)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        req.setStatus(status);
        mentoringRequestRepository.save(req);
    }

    // 일정 제안
    public void proposeSchedule(Long requestId, LocalDateTime proposedAt, User currentUser) {
        MentoringRequest request = findById(requestId);

        if (!request.getUser().equals(currentUser) && !request.getMentor().equals(currentUser)) {
            throw new AccessDeniedException("권한 없음");
        }

        request.setProposedAt(proposedAt);
        request.setScheduleConfirmed(false);
        mentoringRequestRepository.save(request);
    }

    // 일정 확정
    public void confirmSchedule(Long requestId, User currentUser) {
        MentoringRequest request = findById(requestId);

        if (!request.getUser().equals(currentUser) && !request.getMentor().equals(currentUser)) {
            throw new AccessDeniedException("권한 없음");
        }

        if (request.getProposedAt() == null) {
            throw new IllegalStateException("제안된 일정 없음");
        }

        request.setScheduledAt(request.getProposedAt());
        request.setScheduleConfirmed(true);

        if (request.getSessionUrl() == null) {
            request.setSessionUrl(generateSessionLink(request));
        }

        mentoringRequestRepository.save(request);
    }

    // 일정 제안 또는 확정
    public MentoringRequest scheduleMentoring(Long id, LocalDateTime proposedAt, User currentUser) {
        MentoringRequest req = findById(id);
        if (req.getScheduledAt() != null) {
            throw new IllegalStateException("이미 일정이 확정되었습니다.");
        }

        if (req.getProposedAt() == null) {
            req.setProposedAt(proposedAt);
        } else if (req.getProposedAt().equals(proposedAt)) {
            req.setScheduledAt(proposedAt);
            req.setScheduleConfirmed(true);
            req.setSessionUrl(generateSessionLink(req));
        } else {
            throw new IllegalStateException("제안된 일정이 일치하지 않습니다.");
        }

        return mentoringRequestRepository.save(req);
    }

    // 세션 링크 생성
    private String generateSessionLink(MentoringRequest req) {
        return "https://meet.jit.si/mentoring-" + req.getId();
    }

    // 일정 지난 멘토링 자동 완료 처리
    @Scheduled(fixedRate = 60000) // 1분마다 확인
    public void completeExpiredMentoring() {
        List<MentoringRequest> list = mentoringRequestRepository.findAllByStatus(MentoringStatus.ACCEPTED);
        for (MentoringRequest req : list) {
            if (req.getScheduledAt() != null && req.getScheduledAt().isBefore(LocalDateTime.now().minusHours(1))) {
                req.setStatus(MentoringStatus.COMPLETED);
                req.setCompleted(true);
                mentoringRequestRepository.save(req);
            }
        }
    }
}
