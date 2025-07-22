package com.example.portpilot.domain.resume.repository;

import com.example.portpilot.domain.resume.entity.ResumeSection;
import com.example.portpilot.domain.resume.entity.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeSectionRepository extends JpaRepository<ResumeSection, Long> {

    Optional<ResumeSection> findByResumeIdAndSectionType(Long resumeId, SectionType sectionType);
}
