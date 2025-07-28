package com.example.portpilot.adminPage.report;

import com.example.portpilot.domain.board.board.Board;
import com.example.portpilot.domain.board.board.BoardRepository;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final BoardRepository boardRepository;

    @GetMapping
    public String reportList(Model model) {
        List<Report> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        return "admin/report";
    }

    @GetMapping("/{id}")
    public String reportDetail(@PathVariable Long id, Model model) {
        Report report = reportService.getReportById(id);
        model.addAttribute("report", report);

        if (report.getTargetType() == ReportTargetType.BOARD) {
            Board board = boardRepository.findById(report.getTargetId()).orElse(null);
            model.addAttribute("targetBoard", board);
        }

        User reportedUser = reportService.getReportedUser(id);
        model.addAttribute("reportedUser", reportedUser);

        User reporterUser = reportService.getReporterUser(id);
        model.addAttribute("reporterUser", reporterUser);

        return "admin/reportDetail";
    }

}
