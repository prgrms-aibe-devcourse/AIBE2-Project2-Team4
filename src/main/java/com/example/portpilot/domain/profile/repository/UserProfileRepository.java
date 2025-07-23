package com.example.portpilot.domain.profile.repository;

import com.example.portpilot.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
}