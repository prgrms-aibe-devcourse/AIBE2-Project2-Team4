package com.example.portpilot.domain.resume.repository;

import com.example.portpilot.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 사용자별 이력서 목록 (최신순)
    List<Resume> findByUserIdOrderByUpdateTimeDesc(Long userId);

    // 사용자별 특정 이력서 조회
    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    // 상세 조회용
    @Query("SELECT r FROM Resume r " +
            "LEFT JOIN FETCH r.sections " +
            "LEFT JOIN FETCH r.educations " +
            "LEFT JOIN FETCH r.careers " +
            "LEFT JOIN FETCH r.experiences " +
            "WHERE r.id = :id AND r.user.id = :userId")
    Optional<Resume> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);
}
