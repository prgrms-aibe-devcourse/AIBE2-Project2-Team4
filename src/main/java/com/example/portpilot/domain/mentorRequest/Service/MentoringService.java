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

    // 멘티가 신청한 목록
    public List<MentoringRequestResponseDto> getRequestsByUser(User user) {
        return mentoringRequestRepository.findByUser(user).stream()
                .filter(req -> req.getStatus() == MentoringStatus.PENDING)
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 멘토가 받은 신청 목록
    public List<MentoringRequestResponseDto> getRequestsByMentor(User mentor) {
        return mentoringRequestRepository.findByMentor(mentor).stream()
                .filter(req -> req.getStatus() == MentoringStatus.PENDING)
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 수락된 요청 목록
    public List<MentoringRequestResponseDto> getAcceptedRequests(User user) {
        return mentoringRequestRepository.findByUserOrMentor(user, user).stream()
                .filter(req -> req.getStatus() == MentoringStatus.ACCEPTED)
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 완료된 요청 목록
    public List<MentoringRequestResponseDto> getCompletedRequests(User user) {
        return mentoringRequestRepository.findByUserOrMentor(user, user).stream()
                .filter(req -> req.getStatus() == MentoringStatus.COMPLETED)
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ID로 요청 찾기
    public MentoringRequest findById(Long id) {
        MentoringRequest request = mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 존재하지 않습니다."));

        // 차단된 멘토링 접근 차단
        if (request.isBlocked()) {
            throw new IllegalArgumentException("삭제된 멘토링 신청입니다.");
        }

        return request;
    }

    // 신청 수락
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

    // 상태 업데이트
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

        Long currentUserId = currentUser.getId();
        Long menteeId = request.getUser().getId();
        Long mentorId = request.getMentor() != null ? request.getMentor().getId() : null;

        if (!currentUserId.equals(menteeId) && (mentorId == null || !currentUserId.equals(mentorId))) {
            throw new AccessDeniedException("권한 없음");
        }

        request.setProposedAt(proposedAt);
        request.setScheduleConfirmed(false);
        request.setProposedById(currentUserId);
        mentoringRequestRepository.save(request);
    }

    // 일정 확정
    public void confirmSchedule(Long requestId, User currentUser) {
        MentoringRequest request = findById(requestId);

        Long currentUserId = currentUser.getId();
        Long proposedById = request.getProposedById();

        // 제안자는 수락 불가
        if (proposedById != null && currentUserId.equals(proposedById)) {
            throw new AccessDeniedException("자신이 제안한 일정은 수락할 수 없습니다.");
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

    // 일정 저장 또는 확정
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

    // 자동 완료 처리 (멘토링 시간인 scheduledAt을 1시간 지나면 자동 종료 (completed))
    // 단 현재
    @Scheduled(fixedRate = 60000)
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


    // 정식 완료 처리 (상호 동의 방식)
    public void completeMentoring(Long id, User currentUser) {
        MentoringRequest request = findById(id);

        // 권한 체크
        if (!Objects.equals(request.getMentor(), currentUser) &&
                !Objects.equals(request.getUser(), currentUser)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 상태 체크
        if (!request.getStatus().equals(MentoringStatus.ACCEPTED)) {
            throw new IllegalStateException("진행 중인 멘토링만 완료할 수 있습니다.");
        }

        // 완료 요청 필드 추가 (엔티티에 completionRequestedBy 필드 필요)
        // 여기서는 간단히 바로 완료 처리
        request.setStatus(MentoringStatus.COMPLETED);
        request.setCompleted(true);
        mentoringRequestRepository.save(request);
    }

}
