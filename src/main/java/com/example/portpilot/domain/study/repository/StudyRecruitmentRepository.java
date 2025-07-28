package com.example.portpilot.domain.study.repository;

import com.example.portpilot.domain.study.entity.StudyRecruitment;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyRecruitmentRepository extends JpaRepository<StudyRecruitment, Long> {

    // is_blocked 필터링 추가
    @Query("SELECT s FROM StudyRecruitment s WHERE s.user = :user AND s.isBlocked = false")
    List<StudyRecruitment> findByUser(@Param("user") User user);

    @Query("SELECT s FROM StudyRecruitment s WHERE s.closed = :closed AND s.isBlocked = false")
    List<StudyRecruitment> findByClosed(@Param("closed") boolean b);

    @Query("SELECT s FROM StudyRecruitment s WHERE s.completed = true AND s.isBlocked = false")
    List<StudyRecruitment> findByCompletedTrue();

    @Query("SELECT s FROM StudyRecruitment s WHERE s.closed = false AND s.completed = false AND s.isBlocked = false")
    List<StudyRecruitment> findByClosedFalseAndCompletedFalse();

    @Query("SELECT s FROM StudyRecruitment s WHERE s.closed = true AND s.completed = false AND s.isBlocked = false")
    List<StudyRecruitment> findByClosedTrueAndCompletedFalse();
}