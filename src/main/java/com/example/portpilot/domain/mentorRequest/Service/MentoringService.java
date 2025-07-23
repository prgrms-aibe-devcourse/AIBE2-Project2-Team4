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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringService {

    private final MentoringRequestRepository mentoringRequestRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;

    //멘토로 등록
    @Transactional
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

    //신청
    @Transactional
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

    //신청 목록 불로오기
    public List<MentoringRequestResponseDto> getRequestsByUser(User user) {
        return mentoringRequestRepository.findByUser(user).stream()
                .map(MentoringRequestResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    //신청 상세보기
    public MentoringRequest findById(Long id) {
        return mentoringRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신청 정보를 찾을 수 없습니다."));
    }

    // 수락/ 거절
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
}
