package com.example.portpilot.domain.portfolio.repository;

import com.example.portpilot.domain.portfolio.entity.Portfolio;
import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import org.springframework.data.domain.Page;            // ← 추가
import org.springframework.data.domain.Pageable;        // ← 추가
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 특정 사용자의 포트폴리오를 생성일시 내림차순으로 조회
     */
    List<Portfolio> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자, 특정 상태의 포트폴리오 개수 조회
     */
    long countByUserIdAndStatus(Long userId, PortfolioStatus status);

    /**
     * 특정 사용자의 모든 포트폴리오 조회
     */
    List<Portfolio> findByUserId(Long userId);

    /**
     * 전체 포트폴리오 탐색 (검색 + 페이징)
     */
    Page<Portfolio> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}