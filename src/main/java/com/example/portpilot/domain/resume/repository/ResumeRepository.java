package com.example.portpilot.domain.resume.repository;

import com.example.portpilot.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 사용자별 이력서 목록 (최신순)
    List<Resume> findByUserIdOrderByUpdatedAtDesc(Long userId);

    // 사용자별 특정 이력서 조회
    Optional<Resume> findByIdAndUserId(Long id, Long userId);
}
