package com.example.portpilot.domain.resume.repository;

import com.example.portpilot.domain.resume.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, Long> {
}
