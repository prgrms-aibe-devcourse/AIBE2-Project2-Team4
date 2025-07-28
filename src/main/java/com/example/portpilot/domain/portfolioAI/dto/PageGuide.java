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
public class PageGuide {

    private String pageNumber;
    private String title;
    private String description;
    private List<ContentItem> items;
}