package com.example.portpilot.domain.mentorRequest.repository;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, Long> {
    List<MentoringRequest> findByUser(User user);
}
