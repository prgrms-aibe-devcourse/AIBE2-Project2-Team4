package com.example.portpilot.domain.resume.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SectionType {
    GROWTH("성장과정"),
    MOTIVATION("지원동기"),
    GOALS("포부"),
    PERSONALITY("성격의 장단점"),
    EXPERIENCE("경험");

    private final String description;
}