package com.example.portpilot.domain.resume.dto;

import com.example.portpilot.domain.resume.entity.SectionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SectionRequest {
    private SectionType sectionType;
    private String content;
}