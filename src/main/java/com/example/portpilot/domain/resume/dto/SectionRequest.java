package com.example.portpilot.domain.resume.dto;

import com.example.portpilot.domain.resume.entity.SectionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SectionRequest {

    private SectionType sectionType;
    private String content;
}