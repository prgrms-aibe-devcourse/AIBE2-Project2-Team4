package com.example.portpilot.domain.resume.repository;

import com.example.portpilot.domain.resume.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
}
