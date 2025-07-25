package com.example.portpilot.domain.study.repository;

import com.example.portpilot.domain.study.entity.StudyRecruitment;
import com.example.portpilot.domain.study.entity.StudyParticipation;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.study.entity.StudyApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyParticipationRepository extends JpaRepository<StudyParticipation, Long> {

    List<StudyParticipation> findByStudy(StudyRecruitment study);

    List<StudyParticipation> findByStudyAndStatus(StudyRecruitment study, StudyApplyStatus status);

    Optional<StudyParticipation> findByStudyAndUser(StudyRecruitment study, User user);

    List<StudyParticipation> findByUser(User user);
}
