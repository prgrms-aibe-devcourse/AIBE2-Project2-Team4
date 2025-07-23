package com.example.portpilot.domain.mentorRequest.repository;

import com.example.portpilot.domain.mentorRequest.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    Optional<MentorProfile> findByUserId(Long userId);
}
