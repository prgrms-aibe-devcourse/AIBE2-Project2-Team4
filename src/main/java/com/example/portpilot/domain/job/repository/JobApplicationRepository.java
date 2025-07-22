package com.example.portpilot.domain.job.repository;

import com.example.portpilot.domain.job.entity.JobApplication;
import com.example.portpilot.domain.job.entity.JobPosition;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // 특정 사용자의 지원 내역 조회
    List<JobApplication> findByUser(User user);

    // 사용자 ID로 지원 내역 조회
    List<JobApplication> findByUserId(Long userId);

    // 특정 채용공고의 지원자 목록
    List<JobApplication> findByJobPosition(JobPosition jobPosition);

    // 채용공고 ID로 지원자 목록
    List<JobApplication> findByJobPositionId(Long jobPositionId);

    // 상태별 지원 내역 조회
    List<JobApplication> findByStatus(String status);

    // 특정 사용자가 특정 공고에 지원했는지 확인
    Optional<JobApplication> findByUserAndJobPosition(User user, JobPosition jobPosition);

    // 사용자 ID와 채용공고 ID로 지원 여부 확인
    Optional<JobApplication> findByUserIdAndJobPositionId(Long userId, Long jobPositionId);

    // 특정 기간 내 지원 내역
    List<JobApplication> findByAppliedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 채용공고의 상태별 지원자 수 조회
    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPosition.id = :jobPositionId AND ja.status = :status")
    Long countByJobPositionIdAndStatus(@Param("jobPositionId") Long jobPositionId, @Param("status") String status);

    // 사용자의 최근 지원 내역 (최신순)
    List<JobApplication> findByUserIdOrderByAppliedAtDesc(Long userId);
}