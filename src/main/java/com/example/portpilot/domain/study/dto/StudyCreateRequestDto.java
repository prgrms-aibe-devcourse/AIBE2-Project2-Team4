package com.example.portpilot.domain.study.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class StudyCreateRequestDto {
    private String title;
    private String content;
    private int maxMembers;

    private int backendRecruit;
    private int frontendRecruit;
    private int designerRecruit;
    private int plannerRecruit;


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    private List<String> techStacks_BACKEND;
    private List<String> techStacks_FRONTEND;
    private List<String> techStacks_DESIGNER;
    private List<String> techStacks_PLANNER;
}