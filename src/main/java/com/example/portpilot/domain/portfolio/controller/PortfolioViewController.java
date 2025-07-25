package com.example.portpilot.domain.portfolio.controller;

import com.example.portpilot.domain.portfolio.dto.PortfolioResponse;
import com.example.portpilot.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class PortfolioViewController {

    private final PortfolioService portfolioService;

    /**
     * 포트폴리오 탐색 페이지
     * URL: GET /portfolio  또는  GET /portfolio/explore
     */
    @GetMapping({"/portfolio", "/portfolio/explore"})
    public String portfolioview(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model
    ) {
        Page<PortfolioResponse> portPage = portfolioService.searchPortfolios(
                keyword,
                PageRequest.of(page, size)
        );

        model.addAttribute("portfolios", portPage.getContent());
        model.addAttribute("page", portPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("active", "portfolio");

        // 뷰 템플릿:
        return "portfolio/portfolioview";
    }

    /**
     * 컨트롤러가 스캔되고 있는지 확인용 핑 테스트
     * URL: GET /portfolio/ping
     */
    @GetMapping("/portfolio/ping")
    @ResponseBody
    public String ping() {
        return "pong";
    }
}
