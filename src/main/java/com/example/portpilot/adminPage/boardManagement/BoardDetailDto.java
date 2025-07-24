package com.example.portpilot.adminPage.boardManagement;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardDetailDto {
    private Long id;
    private String title;
    private String content;
    private int viewCount;
    private String jobType;
    private String techStack;
    private boolean isBlocked;
    private String imageUrl;
    private LocalDateTime createdAt;

    private Long userId;
    private String userName;
}
