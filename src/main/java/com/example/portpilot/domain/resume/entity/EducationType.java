package com.example.portpilot.domain.resume.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationType {
    HIGH_SCHOOL("고등학교"),
    UNIVERSITY("대학교"),
    GRADUATE_SCHOOL("대학원");

    private final String description;
}