package com.example.portpilot.domain.portfolioAI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentItem {

    private String title;
    private String content;
    private List<String> tips;
}