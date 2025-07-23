package com.example.portpilot.domain.profile.repository;

import com.example.portpilot.domain.profile.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findAllByUserId(Long userId);
    void deleteAllByUserId(Long userId);
}
