package com.example.portpilot.adminPage.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 상태의 신고 목록 조회 (예: PENDING 상태만 보기)
    List<Report> findByStatus(ReportStatus status);

    // 특정 대상에 대한 신고 목록 (예: 특정 게시글, 댓글 등)
    List<Report> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

    // 특정 사용자가 신고한 목록
    List<Report> findByReporterId(Long reporterId);

    // 특정 사용자가 신고당한 목록
    List<Report> findByReportedUserId(Long reportedUserId);

    int countByStatus(ReportStatus status);

}
