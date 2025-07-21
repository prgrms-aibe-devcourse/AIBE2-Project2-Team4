package com.example.portpilot.domain.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findAllByUserId(Long userId);
}