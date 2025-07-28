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
public class PortfolioGuideResponse {

    private String jobCategory;
    private String jobSubcategory;
    private String fileName;
    private List<PageGuide> pages;
}