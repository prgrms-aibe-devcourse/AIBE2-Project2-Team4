package com.example.portpilot.domain.study.repository;

import com.example.portpilot.domain.study.entity.StudyRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface StudyRepository extends JpaRepository<StudyRecruitment, Long> {
    List<StudyRecruitment> findByClosedFalseAndCompletedFalse();

    List<StudyRecruitment> findByClosedFalseAndIsBlockedFalse();
}