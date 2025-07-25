package com.example.portpilot.domain.portfolio.service;

import com.example.portpilot.domain.portfolio.dto.PortfolioRequest;
import com.example.portpilot.domain.portfolio.dto.PortfolioResponse;
import com.example.portpilot.domain.portfolio.entity.Portfolio;
import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import com.example.portpilot.domain.portfolio.repository.PortfolioRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserService userService;

    @Transactional
    public PortfolioResponse createPortfolio(Long userId, PortfolioRequest req) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 사용자 ID: " + userId));

        Portfolio p = Portfolio.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .link(req.getLink())
                .createdAt(LocalDateTime.now())
                .status(PortfolioStatus.PENDING_REVIEW)
                .user(user)
                .build();

        return toResponse(portfolioRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getUserPortfolios(Long userId) {
        userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 사용자 ID: " + userId));
        return portfolioRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 전체 포트폴리오 탐색 (키워드 검색 + 페이징)
     */
    @Transactional(readOnly = true)
    public Page<PortfolioResponse> searchPortfolios(String keyword, Pageable pageable) {
        Page<Portfolio> page;
        if (keyword == null || keyword.isBlank()) {
            page = portfolioRepository.findAll(pageable);
        } else {
            page = portfolioRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        return page.map(this::toResponse);
    }

    private PortfolioResponse toResponse(Portfolio p) {
        return PortfolioResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .link(p.getLink())
                .createdAt(p.getCreatedAt())
                .status(p.getStatus())
                .build();
    }
}