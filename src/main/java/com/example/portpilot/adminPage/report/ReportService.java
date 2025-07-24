package com.example.portpilot.adminPage.report;

import com.example.portpilot.adminPage.admin.Admin;
import com.example.portpilot.adminPage.admin.AdminRepository;
import com.example.portpilot.domain.board.board.Board;
import com.example.portpilot.domain.board.board.BoardRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import com.example.portpilot.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 신고를 찾을 수 없습니다."));
    }

    @Transactional
    public void resolveReport(Long reportId, ReportStatus status) {
        Report report = getReportById(reportId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            throw new NotFoundException("관리자를 찾을 수 없습니다.");
        }

        report.setStatus(status);
        report.setResolvedAt(LocalDateTime.now());
        report.setReportAdmin(admin);

        if (status == ReportStatus.RESOLVED && report.getTargetType() == ReportTargetType.BOARD) {
            Board board = boardRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));
            board.setBlocked(true);

            // 게시글 관련 모든 신고 일괄 RESOLVED 처리
            resolveReportsForBlockedBoard(board.getId());
        }

        reportRepository.save(report);
    }

    @Transactional
    public void resolveReportsForBlockedBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));
        if (!board.isBlocked()) {
            throw new IllegalStateException("해당 게시글은 차단되지 않았습니다.");
        }

        List<Report> reports = reportRepository.findByTargetTypeAndTargetId(
                ReportTargetType.BOARD, boardId);

        for (Report report : reports) {
            if (report.getStatus() != ReportStatus.RESOLVED) {
                report.setStatus(ReportStatus.RESOLVED);
                report.setResolvedAt(LocalDateTime.now());
            }
        }
    }

    public User getReportedUser(Long reportId) {
        return getReportById(reportId).getReportedUser();
    }

    public User getReporterUser(Long reportId) {
        return getReportById(reportId).getReporter();
    }

    @Transactional
    public void reportBoard(Long boardId, ReportRequestDto dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 존재하지 않습니다."));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User reporter = userRepository.findByEmail(email);
        if (reporter == null) {
            throw new NotFoundException("신고자를 찾을 수 없습니다.");
        }

        Report report = Report.builder()
                .targetType(ReportTargetType.BOARD)
                .targetId(board.getId())
                .reportedUser(board.getUser()) // 변경된 부분
                .reporter(reporter)            // 변경된 부분
                .reason(dto.getReason())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);
    }
}
