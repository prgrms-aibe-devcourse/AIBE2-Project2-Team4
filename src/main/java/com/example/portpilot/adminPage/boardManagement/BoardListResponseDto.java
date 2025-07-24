package com.example.portpilot.adminPage.boardManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BoardListResponseDto {
    private List<BoardDetailDto> boards;
    private int totalPages;
}
