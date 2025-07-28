package com.example.portpilot.domain.mentorReview.service;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.mentorRequest.repository.MentoringRequestRepository;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewCreateDto;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewResponseDto;
import com.example.portpilot.domain.mentorReview.dto.MentoringReviewUpdateDto;
import com.example.portpilot.domain.mentorReview.entity.MentoringReview;
import com.example.portpilot.domain.mentorReview.repository.MentoringReviewRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringReviewService {

    private final MentoringReviewRepository mentoringReviewRepository;
    private final MentoringRequestRepository mentoringRequestRepository;
    private final UserRepository userRepository;

    // 후기 작성
    public void createReview(MentoringReviewCreateDto dto, User reviewer) {
        MentoringRequest mentoringRequest = mentoringRequestRepository.findById(dto.getMentoringRequestId())
                .orElseThrow(() -> new IllegalArgumentException("멘토링 요청을 찾을 수 없습니다."));

        if (!mentoringRequest.getUser().getId().equals(reviewer.getId())) {
            throw new AccessDeniedException("본인이 참여한 멘토링에만 후기를 작성할 수 있습니다.");
        }

        if (!mentoringRequest.getStatus().equals(MentoringStatus.COMPLETED)) {
            throw new IllegalStateException("완료된 멘토링에만 후기를 작성할 수 있습니다.");
        }

        if (mentoringReviewRepository.findByMentoringRequestId(dto.getMentoringRequestId()).isPresent()) {
            throw new IllegalStateException("이미 해당 멘토링에 대한 후기를 작성하셨습니다.");
        }

        MentoringReview review = MentoringReview.builder()
                .mentoringRequestId(dto.getMentoringRequestId())
                .reviewer(reviewer)
                .mentor(mentoringRequest.getMentor())
                .title(dto.getTitle())
                .content(dto.getContent())
                .rating(dto.getRating())
                .viewCount(0)
                .build();

        mentoringReviewRepository.save(review);
    }

    // 전체 후기 목록 조회
    @Transactional(readOnly = true)
    public List<MentoringReviewResponseDto> getAllReviews() {
        return mentoringReviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(MentoringReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 후기 상세 조회 (조회수 증가 및 작성자 여부 판단)
    @Transactional
    public MentoringReviewResponseDto getReviewById(Long id) {
        MentoringReview review = mentoringReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));

        // 차단된 후기 접근 차단
        if (review.is_blocked()) {
            throw new IllegalArgumentException("삭제된 후기입니다.");
        }

        review.incrementViewCount();
        mentoringReviewRepository.save(review);

        MentoringReviewResponseDto dto = MentoringReviewResponseDto.fromEntity(review);
        User currentUser = getCurrentUser();

        // 현재 유저가 작성자인 경우 표시
        if (currentUser != null && review.getReviewer().getId().equals(currentUser.getId())) {
            dto.setWriter(true);
        } else {
            dto.setWriter(false);
        }

        return dto;
    }

    // 작성 가능한 후기 대상 멘토링 조회
    @Transactional(readOnly = true)
    public List<MentoringRequest> getCompletedMentoringsByUser(User user) {
        return mentoringRequestRepository.findByUser(user)
                .stream()
                .filter(request -> request.getStatus().equals(MentoringStatus.COMPLETED))
                .filter(request -> mentoringReviewRepository.findByMentoringRequestId(request.getId()).isEmpty())
                .collect(Collectors.toList());
    }

    // 후기 수정
    @Transactional
    public void updateReview(Long reviewId, MentoringReviewUpdateDto dto, User currentUser) {
        MentoringReview review = mentoringReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));

        // 차단된 후기 접근 차단
        if (review.is_blocked()) {
            throw new IllegalArgumentException("삭제된 후기입니다.");
        }

        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인이 작성한 후기만 수정할 수 있습니다.");
        }

        review.setTitle(dto.getTitle());
        review.setContent(dto.getContent());
        review.setRating(dto.getRating());

        mentoringReviewRepository.save(review);
    }

    // 후기 삭제
    @Transactional
    public void deleteReview(Long reviewId, User currentUser) {
        MentoringReview review = mentoringReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));

        // 차단된 후기 접근 차단
        if (review.is_blocked()) {
            throw new IllegalArgumentException("삭제된 후기입니다.");
        }

        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인이 작성한 후기만 삭제할 수 있습니다.");
        }

        mentoringReviewRepository.delete(review);
    }

    // 멘토의 후기 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public List<MentoringReviewResponseDto> getReviewsByMentor(Long mentorId, Pageable pageable) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException("멘토를 찾을 수 없습니다."));

        Page<MentoringReview> reviewPage = mentoringReviewRepository.findByMentorOrderByCreatedAtDesc(mentor, pageable);
        return reviewPage.getContent()
                .stream()
                .map(MentoringReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 멘티의 후기 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public List<MentoringReviewResponseDto> getReviewsByMentee(Long menteeId, Pageable pageable) {
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("멘티를 찾을 수 없습니다."));

        Page<MentoringReview> reviewPage = mentoringReviewRepository.findByReviewerOrderByCreatedAtDesc(mentee, pageable);
        return reviewPage.getContent()
                .stream()
                .map(MentoringReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 전체 후기 페이징 조회
    @Transactional(readOnly = true)
    public Page<MentoringReviewResponseDto> getAllReviewsPaged(Pageable pageable) {
        return mentoringReviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(MentoringReviewResponseDto::fromEntity);
    }

    // 후기 소유 여부 확인
    @Transactional(readOnly = true)
    public boolean isReviewOwner(Long reviewId, User user) {
        return mentoringReviewRepository.findById(reviewId)
                .map(review -> review.getReviewer().getId().equals(user.getId()))
                .orElse(false);
    }

    // 현재 로그인한 사용자 반환
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByEmail(auth.getName());
    }
}
