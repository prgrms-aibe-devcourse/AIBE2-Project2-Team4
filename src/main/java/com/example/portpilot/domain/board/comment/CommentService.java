package com.example.portpilot.domain.board.comment;

import com.example.portpilot.domain.board.board.Board;
import com.example.portpilot.domain.board.board.BoardRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long boardId, CommentRequestDto dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(dto.getContent())
                .parentId(dto.getParentId())
                .isBlocked(false)
                .build();

        commentRepository.save(comment);
    }

    @Transactional
    public List<CommentResponseDto> getCommentsByBoardId(Long boardId, User currentUser) {
        List<Comment> comments = commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);
        return comments.stream()
                .map(comment -> CommentResponseDto.fromEntity(comment, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateComment(Long commentId, CommentRequestDto dto, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.setContent(dto.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
