package com.example.portpilot.domain.portfolio.controller;

import com.example.portpilot.domain.portfolio.dto.PortfolioRequest;
import com.example.portpilot.domain.portfolio.dto.PortfolioResponse;
import com.example.portpilot.domain.portfolio.service.PortfolioService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    /** 1) 내 포트폴리오 목록 조회 */
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> listMine(Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<PortfolioResponse> list = portfolioService.getUserPortfolios(user.getId());
        return ResponseEntity.ok(list);
    }

    /** 2) 새 포트폴리오 생성 */
    @PostMapping
    public ResponseEntity<PortfolioResponse> create(
            @Validated @RequestBody PortfolioRequest request,
            Principal principal
    ) {
        User user = userRepository.findByEmail(principal.getName());
        PortfolioResponse resp = portfolioService.createPortfolio(user.getId(), request);
        return ResponseEntity.ok(resp);
    }

    /** 3) 전체 포트폴리오 탐색 (검색 + 페이징) */
    @GetMapping("/explore")
    public ResponseEntity<Page<PortfolioResponse>> explore(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<PortfolioResponse> results = portfolioService.searchPortfolios(
                keyword,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(results);
    }
}
