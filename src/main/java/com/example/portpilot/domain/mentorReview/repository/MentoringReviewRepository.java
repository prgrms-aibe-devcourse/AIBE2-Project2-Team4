package com.example.portpilot.domain.mentorReview.repository;

import com.example.portpilot.domain.mentorReview.entity.MentoringReview;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentoringReviewRepository extends JpaRepository<MentoringReview, Long> {

    // 최신순으로 모든 멘토링 후기 조회
    List<MentoringReview> findAllByOrderByCreatedAtDesc();

    // 특정 멘토링에 대한 후기 조회
    Optional<MentoringReview> findByMentoringRequestId(Long mentoringRequestId);

    // 특정 멘토가 받은 후기들
    List<MentoringReview> findByMentorOrderByCreatedAtDesc(User mentor);

    // 특정 사용자가 작성한 후기들
    List<MentoringReview> findByReviewerOrderByCreatedAtDesc(User reviewer);

}