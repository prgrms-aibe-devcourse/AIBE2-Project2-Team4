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

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> listMine(Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        return ResponseEntity.ok(portfolioService.getUserPortfolios(user.getId()));
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> create(
            @Validated @RequestBody PortfolioRequest request,
            Principal principal
    ) {
        User user = userRepository.findByEmail(principal.getName());
        return ResponseEntity.ok(portfolioService.createPortfolio(user.getId(), request));
    }

    @GetMapping("/explore")
    public ResponseEntity<Page<PortfolioResponse>> explore(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<PortfolioResponse> results = portfolioService.searchPortfolios(
                keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(results);
    }
}