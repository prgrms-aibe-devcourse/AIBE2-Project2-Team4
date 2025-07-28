package com.example.portpilot.domain.mentorRequest.repository;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {
    // 내가 멘티로 신청한 내역
    @Query("SELECT mr FROM MentoringRequest mr WHERE mr.user = :user AND mr.isBlocked = false")
    List<MentoringRequest> findByUser(@Param("user") User user);

    // 멘토링 상태별 조회
    @Query("SELECT mr FROM MentoringRequest mr WHERE mr.status = :status AND mr.isBlocked = false")
    List<MentoringRequest> findAllByStatus(@Param("status") MentoringStatus mentoringStatus);

    // 내가 멘토로 받은 신청 내역
    @Query("SELECT mr FROM MentoringRequest mr WHERE mr.mentor = :mentor AND mr.isBlocked = false")
    List<MentoringRequest> findByMentor(@Param("mentor") User mentor);

    // 사용자가 관련된 모든 멘토링
    @Query("SELECT mr FROM MentoringRequest mr WHERE (mr.user = :user OR mr.mentor = :mentor) AND mr.isBlocked = false")
    List<MentoringRequest> findByUserOrMentor(@Param("user") User user, @Param("mentor") User mentor);

    // 상태별 카운트
    @Query("SELECT COUNT(mr) FROM MentoringRequest mr WHERE mr.status = :status AND mr.isBlocked = false")
    long countByStatus(@Param("status") MentoringStatus status);
}
