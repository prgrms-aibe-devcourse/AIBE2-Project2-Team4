package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    // boardList 조회 메서드
    @GetMapping
    public String boardList(@RequestParam(required = false) String jobType,
                            @RequestParam(required = false) String techStack,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {

        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoardResponseDto> boards = boardService.getBoardsFiltered(jobType, techStack, keyword, pageable);

        model.addAttribute("boards", boards);
        model.addAttribute("jobType", jobType);
        model.addAttribute("techStack", techStack);
        model.addAttribute("keyword", keyword);
        return "board/boardList";
    }


    // 게시글 작성
    @PostMapping
    public String createBoard(@ModelAttribute BoardRequestDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return "redirect:/login"; // 로그인 안 돼있으면 로그인 페이지로
        }

        boardService.create(dto, user);
        return "redirect:/board";
    }

    // 게시글 상세조회 + 조회수 증가
    @GetMapping("/{id}")
    public String boardDetail(@PathVariable Long id, Model model) {
        BoardResponseDto dto = boardService.getById(id);
        model.addAttribute("board", dto);
        return "board/boardDetail";
    }

    // 수정 폼 조회
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        BoardResponseDto dto = boardService.getById(id);
        model.addAttribute("board", dto);
        return "board/boardEdit";
    }

    // 게시글 수정
    @PostMapping("/edit/{id}")
    public String updateBoard(@PathVariable Long id, @ModelAttribute BoardRequestDto dto) {
        boardService.updateBoard(id, dto);
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    @PostMapping("/delete/{id}")
    public String deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return "redirect:/board";
    }
}
