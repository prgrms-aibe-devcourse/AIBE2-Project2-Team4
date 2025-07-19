package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardApiController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    //게시글 목록
    @GetMapping
    public ResponseEntity<Page<BoardResponseDto>> getBoards(
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String techStack,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoardResponseDto> boards = boardService.getBoardsFiltered(jobType, techStack, keyword, pageable);
        return ResponseEntity.ok(boards);
    }

    // 게시글
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoardDetail(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.viewBoard(id));
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<Void> createBoard(@RequestBody BoardRequestDto dto, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);

        if (user == null) return ResponseEntity.status(401).build(); // Unauthorized
        if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getContent() == null || dto.getContent().isBlank()) {
            return ResponseEntity.badRequest().build(); // Bad Request
        }

        boardService.create(dto, user);
        return ResponseEntity.status(201).build(); // Created
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto dto) {
        boardService.updateBoard(id, dto);
        return ResponseEntity.ok().build();
    }

    // 게시글 삭제하기
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.ok().build();
    }

    // 내 게시글
    @GetMapping("/my")
    public ResponseEntity<Page<BoardResponseDto>> getMyBoards(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = auth.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoardResponseDto> myBoards = boardService.getMyBoards(email, pageable);
        return ResponseEntity.ok(myBoards);
    }

}
