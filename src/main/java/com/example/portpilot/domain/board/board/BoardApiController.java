package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardApiController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    @GetMapping
    public Page<BoardResponseDto> getFilteredBoards(
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // 빈 문자열을 null로 변환
        if (jobType != null && jobType.isBlank()) jobType = null;
        if (techStack != null && techStack.isBlank()) techStack = null;
        if (keyword != null && keyword.isBlank()) keyword = null;

        return boardService.getBoardsFiltered(jobType, techStack, keyword, pageable);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<BoardResponseDto>> getMyBoards(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              Authentication auth) {
        String email = auth.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(boardService.getMyBoards(email, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.viewBoard(id));
    }

    @PostMapping
    public ResponseEntity<Void> createBoard(@RequestBody BoardRequestDto dto, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        boardService.create(dto, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> updateBoard(@PathVariable Long id,
                                       @RequestBody BoardRequestDto dto,
                                       Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        boardService.updateBoard(id, dto, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id,
                                       Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        boardService.deleteBoard(id, user);
        return ResponseEntity.ok().build();
    }

}
