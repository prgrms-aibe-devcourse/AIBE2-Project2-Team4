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

    List<MentoringReview> findAllByOrderByCreatedAtDesc();
    Optional<MentoringReview> findByMentoringRequestId(Long mentoringRequestId);
    List<MentoringReview> findByMentorOrderByCreatedAtDesc(User mentor);
    List<MentoringReview> findByReviewerOrderByCreatedAtDesc(User reviewer);
    Long countByMentor(User mentor);
    Long countByReviewer(User reviewer);

    @Query("SELECT AVG(r.rating) FROM MentoringReview r WHERE r.mentor = :mentor")
    Double findAverageRatingByMentor(@Param("mentor") User mentor);

    @Query("SELECT COUNT(r) FROM MentoringReview r WHERE r.mentor = :mentor")
    Long countReviewsByMentor(@Param("mentor") User mentor);

    // 새로 추가할 페이징 메소드들
    Page<MentoringReview> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<MentoringReview> findByMentorOrderByCreatedAtDesc(User mentor, Pageable pageable);
    Page<MentoringReview> findByReviewerOrderByCreatedAtDesc(User reviewer, Pageable pageable);
}