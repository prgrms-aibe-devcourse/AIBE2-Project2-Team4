package com.example.portpilot.domain.board.comment;

import com.example.portpilot.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentResponseDto {
    private final Long id;
    private final String userName;
    private final String content;
    private final String createdAt;
    private final boolean editable;

    @Builder
    public CommentResponseDto(Long id, String userName, String content, String createdAt, boolean editable) {
        this.id = id;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.editable = editable;
    }

    public static CommentResponseDto fromEntity(Comment comment, User currentUser) {
        boolean isEditable = comment.getUser().getId().equals(currentUser.getId())
                || currentUser.getRole().equals("ADMIN");

        return CommentResponseDto.builder()
                .id(comment.getId())
                .userName(comment.getUser().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .editable(isEditable)
                .build();
    }
}
