package com.example.portpilot.domain.board.comment;

import com.example.portpilot.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentResponseDto {
    private Long id;
    private String userName;
    private String content;
    private String createdAt;
    private boolean editable;

    public static CommentResponseDto fromEntity(Comment comment, User currentUser) {
        boolean isEditable = comment.getUser().getId().equals(currentUser.getId())
                || currentUser.getRole().equals("ADMIN");

        return new CommentResponseDto(
                comment.getId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreatedAt().toString(),
                isEditable
        );
    }

    public CommentResponseDto(Long id, String userName, String content, String createdAt, boolean editable) {
        this.id = id;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.editable = editable;
    }
}