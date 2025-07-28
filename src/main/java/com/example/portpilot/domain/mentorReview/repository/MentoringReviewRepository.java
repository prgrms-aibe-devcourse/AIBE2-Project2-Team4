package com.example.portpilot.domain.mentorReview.repository;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorReview.entity.MentoringReview;
import com.example.portpilot.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentoringReviewRepository extends JpaRepository<MentoringReview, Long> {

    // 전체 후기 조회
    @Query("SELECT mr FROM MentoringReview mr WHERE mr.is_blocked = false ORDER BY mr.createdAt DESC")
    List<MentoringReview> findAllByOrderByCreatedAtDesc();

    // 멘토링 요청 ID로 후기 찾기
    @Query("SELECT mr FROM MentoringReview mr WHERE mr.mentoringRequestId = :mentoringRequestId AND mr.is_blocked = false")
    Optional<MentoringReview> findByMentoringRequestId(@Param("mentoringRequestId") Long mentoringRequestId);

    // 페이징 메소드들
    @Query("SELECT mr FROM MentoringReview mr WHERE mr.is_blocked = false ORDER BY mr.createdAt DESC")
    Page<MentoringReview> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT mr FROM MentoringReview mr WHERE mr.mentor = :mentor AND mr.is_blocked = false ORDER BY mr.createdAt DESC")
    Page<MentoringReview> findByMentorOrderByCreatedAtDesc(@Param("mentor") User mentor, Pageable pageable);

    @Query("SELECT mr FROM MentoringReview mr WHERE mr.reviewer = :reviewer AND mr.is_blocked = false ORDER BY mr.createdAt DESC")
    Page<MentoringReview> findByReviewerOrderByCreatedAtDesc(@Param("reviewer") User reviewer, Pageable pageable);
}