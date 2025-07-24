package com.example.portpilot.adminPage.dashboard;

import com.example.portpilot.domain.mentorRequest.MentoringRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {
    long countByStatus(MentoringRequest.RequestStatus status);
}