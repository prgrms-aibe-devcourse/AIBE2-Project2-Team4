package com.example.portpilot.domain.mentorReview.service;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.mentorRequest.repository.MentoringRequestRepository;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewCreateDto;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewResponseDto;
import com.example.portpilot.domain.mentorReview.entity.MentoringReview;
import com.example.portpilot.domain.mentorReview.repository.MentoringReviewRepository;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringReviewService {

    private final MentoringReviewRepository reviewRepository;
    private final MentoringRequestRepository mentoringRequestRepository;

    // 후기 작성
    public void createReview(MentoringReviewCreateDto dto, User reviewer) {
        // 멘토링 요청 조회
        MentoringRequest mentoringRequest = mentoringRequestRepository.findById(dto.getMentoringRequestId())
                .orElseThrow(() -> new IllegalArgumentException("멘토링 요청을 찾을 수 없습니다."));

        // 멘티만 작성 가능
        if (!mentoringRequest.getUser().getId().equals(reviewer.getId())) {
            throw new AccessDeniedException("본인이 참여한 멘토링에만 후기를 작성할 수 있습니다.");
        }

        // 완료된 멘토링인지 확인
        if (!mentoringRequest.getStatus().equals(MentoringStatus.COMPLETED)) {
            throw new IllegalStateException("완료된 멘토링에만 후기를 작성할 수 있습니다.");
        }

        // 이미 후기가 있는지 확인
        if (reviewRepository.findByMentoringRequestId(dto.getMentoringRequestId()).isPresent()) {
            throw new IllegalStateException("이미 해당 멘토링에 대한 후기를 작성하셨습니다.");
        }

        // 후기 생성
        MentoringReview review = MentoringReview.builder()
                .mentoringRequestId(dto.getMentoringRequestId())
                .reviewer(reviewer)
                .mentor(mentoringRequest.getMentor())
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .viewCount(0)
                .build();

        reviewRepository.save(review);
    }

    // 모든 후기 조회
    @Transactional(readOnly = true)
    public List<MentoringReviewResponseDto> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(MentoringReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 후기 상세 조회 (조회수 증가)
    @Transactional
    public MentoringReviewResponseDto getReviewById(Long id) {
        MentoringReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));

        // 조회수 증가
        review.incrementViewCount();
        reviewRepository.save(review);

        return MentoringReviewResponseDto.fromEntity(review);
    }

    // 사용자가 작성 가능한 멘토링 목록 조회
    @Transactional(readOnly = true)
    public List<MentoringRequest> getCompletedMentoringsByUser(User user) {
        return mentoringRequestRepository.findByUser(user)
                .stream()
                .filter(request -> request.getStatus().equals(MentoringStatus.COMPLETED))
                .filter(request -> reviewRepository.findByMentoringRequestId(request.getId()).isEmpty())
                .collect(Collectors.toList());
    }
}