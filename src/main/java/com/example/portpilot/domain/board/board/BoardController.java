package com.example.portpilot.domain.board.board;

import com.example.portpilot.adminPage.report.ReportRequestDto;
import com.example.portpilot.adminPage.report.ReportService;
import com.example.portpilot.adminPage.report.ReportStatus;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;
    private final ReportService reportService;

    @GetMapping
    public String boardList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String jobType,
                            @RequestParam(required = false) String techStack,
                            @RequestParam(required = false) String keyword,
                            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoardResponseDto> boards = boardService.getBoardsFiltered(jobType, techStack, keyword, pageable);

        model.addAttribute("boards", boards);
        model.addAttribute("jobType", jobType);
        model.addAttribute("techStack", techStack);
        model.addAttribute("keyword", keyword);
        return "board/boardList";
    }

    @GetMapping("/{id}")
    public String boardDetail(@PathVariable Long id, Model model) {
        BoardResponseDto dto = boardService.viewBoard(id);
        model.addAttribute("board", dto);
        return "board/boardDetail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        BoardResponseDto dto = boardService.getById(id);
        model.addAttribute("board", dto);
        return "board/boardEdit";
    }

    @PostMapping("/report/{boardId}")
    @ResponseBody
    public String reportBoard(@PathVariable Long boardId, @RequestBody ReportRequestDto dto) {
        reportService.reportBoard(boardId, dto);
        return "신고가 접수되었습니다.";
    }

    @PostMapping("/{id}/resolve")
    public String resolveReport(@PathVariable Long id,
                                @RequestParam ReportStatus status) {
        reportService.resolveReport(id, status);
        return "redirect:/admin/report";
    }
}
