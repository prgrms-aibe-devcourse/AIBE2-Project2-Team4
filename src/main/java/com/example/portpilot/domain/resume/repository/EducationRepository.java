package com.example.portpilot.domain.resume.repository;

import com.example.portpilot.domain.resume.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
}
