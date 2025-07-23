package com.example.portpilot.domain.board.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    private Long boardId;
    private Long parentId;
    private String content;
}