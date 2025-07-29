package com.example.portpilot.domain.job.repository;

import com.example.portpilot.domain.job.entity.JobPosition;
import com.example.portpilot.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {

    // 특정 회사의 채용공고 조회
    List<JobPosition> findByCompany(Company company);

    // 회사 ID로 채용공고 조회
    List<JobPosition> findByCompanyId(Long companyId);

    // 제목에 키워드가 포함된 채용공고 검색
    List<JobPosition> findByTitleContaining(String keyword);

    // 지역으로 채용공고 검색
    List<JobPosition> findByLocationContaining(String location);

    // 상태로 채용공고 검색 (모집중/마감)
    List<JobPosition> findByStatus(String status);

    // 경력 수준으로 검색
    List<JobPosition> findByExperienceLevel(String experienceLevel);

    // 마감일이 특정 날짜 이후인 채용공고
    List<JobPosition> findByDeadlineAfter(LocalDateTime date);

    // 복합 검색: 제목 + 지역 + 상태
    @Query("SELECT jp FROM JobPosition jp WHERE " +
            "jp.title LIKE %:title% AND " +
            "jp.location LIKE %:location% AND " +
            "jp.status = :status")
    List<JobPosition> findByTitleAndLocationAndStatus(
            @Param("title") String title,
            @Param("location") String location,
            @Param("status") String status);

    // 특정 회사의 모집중인 채용공고만
    List<JobPosition> findByCompanyIdAndStatus(Long companyId, String status);

    
}