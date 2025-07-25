package com.example.portpilot.domain.study.repository;

import com.example.portpilot.domain.study.entity.StudyRecruitment;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyRecruitmentRepository extends JpaRepository<StudyRecruitment, Long> {

    @Query("SELECT s FROM StudyRecruitment s JOIN FETCH s.user")
    List<StudyRecruitment> findAllWithUser();

    @Query("SELECT s FROM StudyRecruitment s JOIN FETCH s.user WHERE s.closed = :closed")
    List<StudyRecruitment> findByClosedWithUser(boolean closed);

    @Query("SELECT s FROM StudyRecruitment s JOIN FETCH s.user WHERE s.user = :user")
    List<StudyRecruitment> findByUserWithUser(User user);

    @Query("SELECT s FROM StudyRecruitment s JOIN FETCH s.user WHERE s.id = :id")
    Optional<StudyRecruitment> findByIdWithUser(Long id);

    // 기존 메서드들
    List<StudyRecruitment> findByUser(User user);
    List<StudyRecruitment> findByClosed(boolean b);
}