package com.example.portpilot.domain.mentorRequest.repository;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.mentorRequest.entity.MentoringStatus;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {
    // 내가 멘티로 신청한 내역
    List<MentoringRequest> findByUser(User user);

    // 멘토링 상태
    List<MentoringRequest> findAllByStatus(MentoringStatus mentoringStatus);

    // 내가 멘토로 받은 신청 내역
    List<MentoringRequest> findByMentor(User mentor);

    List<MentoringRequest> findByUserOrMentor(User user, User mentor);


    // 멘토로서 받은 신청 중 ACCEPTED 또는 COMPLETED
    List<MentoringRequest> findByMentorAndStatusIn(User mentor, List<MentoringStatus> statuses);

    // 멘티로 신청한 내역 중 ACCEPTED 또는 COMPLETED
    List<MentoringRequest> findByUserAndStatusIn(User user, List<MentoringStatus> statuses);
}
