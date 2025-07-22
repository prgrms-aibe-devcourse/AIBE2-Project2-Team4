package com.example.portpilot.domain.resume.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResumeStatus {
    COMPLETED("완료"),
    IN_PROGRESS("작성중"),
    TEMP_SAVED("임시저장");

    private final String description;
}