package com.example.portpilot.domain.board.comment;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommentApiController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    @PostMapping("/{boardId}/comments")
    public ResponseEntity<Void> createComment(@PathVariable Long boardId,
                                              @RequestBody CommentRequestDto dto,
                                              Authentication auth) {
        String email = auth.getName();

        commentService.createComment(boardId, dto, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boardId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComment(@PathVariable Long boardId, Authentication auth) {
        String userEmail = auth.getName();
        User user = userRepository.findByEmail(userEmail);
        List<CommentResponseDto> comments = commentService.getCommentsByBoardId(boardId, user);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long boardId,
                                           @PathVariable Long commentId,
                                           @RequestBody CommentRequestDto requestDto,
                                           Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        commentService.updateComment(commentId, requestDto, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long boardId, @PathVariable Long commentId, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().build();
    }
}