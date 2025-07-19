package com.example.portpilot.domain.board.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequestDto {
    //게시판 생성 폼
    private String title;
    private String content;
    private String jobType;
    private String techStack;

    public Board toEntity() {
        return Board.builder()
                .title(title)
                .content(content)
                .jobType(jobType)
                .techStack(techStack)
                .build();
    }
}
