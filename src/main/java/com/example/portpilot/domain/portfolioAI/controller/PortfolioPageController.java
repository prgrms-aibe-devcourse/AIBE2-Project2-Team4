package com.example.portpilot.domain.portfolioAI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortfolioPageController {

    // 포트폴리오 개요 생성 페이지
    @GetMapping("/portfolio/guide")
    public String portfolioOutlinePage() {
        return "portfolio-ai/portfolio-guide";
    }
}