package com.example.portpilot.adminPage.boardManagement;

import com.example.portpilot.domain.board.board.Board;
import com.example.portpilot.domain.board.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board-management")
public class BoardManagementController {

    private final BoardService boardService;

    @GetMapping
    public String showBoardPage() {
        return "admin/boardManagement";
    }

    @GetMapping("/list")
    @ResponseBody
    public BoardListResponseDto listBoards(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "") String keyword) {
        PageRequest pageable = PageRequest.of(page - 1, 10);
        Page<BoardDetailDto> result = boardService.searchBoards(keyword, pageable);
        return new BoardListResponseDto(result.getContent(), result.getTotalPages());
    }


    @PostMapping("/block")
    @ResponseBody
    public void updateBlockedStatus(@RequestBody BoardBlockRequestDto dto) {
        boardService.updateBlockedStatus(dto.getBoardId(), dto.isBlocked());
    }

    @PostMapping("/block/form")
    public String updateBlockStatusFromForm(@RequestParam Long boardId,
                                            @RequestParam boolean isBlocked) {
        boardService.updateBlockedStatus(boardId, isBlocked);
        return "redirect:/admin/board-management/" + boardId;
    }


    @GetMapping("/{id}")
    public String viewBoardDetail(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        model.addAttribute("board", board);
        return "admin/boardManagementDetail";
    }

}
