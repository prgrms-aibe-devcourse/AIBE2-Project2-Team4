package com.example.portpilot.domain.portfolioAI.controller;

import com.example.portpilot.domain.portfolioAI.dto.PortfolioGuideRequest;
import com.example.portpilot.domain.portfolioAI.dto.PortfolioGuideResponse;
import com.example.portpilot.domain.portfolioAI.service.PortfolioGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioGuideController {

    private final PortfolioGuideService portfolioGuideService;

    // 개인화된 포트폴리오 개요 생성
    @PostMapping("/analyze")
    public ResponseEntity<?> generatePersonalizedOutline(@ModelAttribute @Valid PortfolioGuideRequest request) {
        try {
            log.info("개인화된 포트폴리오 개요 생성 요청 - 파일: {}, 직무: {} > {}",
                    request.getFile().getOriginalFilename(),
                    request.getJobCategory(),
                    request.getJobSubcategory());

            PortfolioGuideResponse response = portfolioGuideService.generateOutline(request);

            log.info("개인화된 포트폴리오 개요 생성 완료 - 페이지 수: {}", response.getPages().size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포트폴리오 개요 생성 오류", e);
            return ResponseEntity.badRequest().body("개요 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}