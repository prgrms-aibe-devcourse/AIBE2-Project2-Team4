package com.example.portpilot.adminPage.boardManagement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardBlockRequestDto {
    private Long boardId;
    private boolean isBlocked;
}
