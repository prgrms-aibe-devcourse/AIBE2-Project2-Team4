package com.example.portpilot.domain.study.repository;

import com.example.portpilot.domain.study.StudyRecruitment;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyRecruitmentRepository extends JpaRepository<StudyRecruitment, Long> {

    List<StudyRecruitment> findByUser(User user);
}
