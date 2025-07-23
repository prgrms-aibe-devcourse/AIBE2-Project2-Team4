package com.example.portpilot.domain.board.board;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardResponseDto {
    private Long id;
    private String title;
    private String content;
    private String userName;
    private String jobType;
    private String techStack;
    private int viewCount;
    private LocalDateTime createdAt;

    public static BoardResponseDto fromEntity(Board board) {
        BoardResponseDto dto = new BoardResponseDto();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setJobType(board.getJobType());
        dto.setTechStack(board.getTechStack());
        dto.setViewCount(board.getViewCount());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setUserName(board.getUser() != null ? board.getUser().getName() : "익명");
        return dto;
    }
}
