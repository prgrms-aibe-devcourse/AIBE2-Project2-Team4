package com.example.portpilot.domain.portfolio.repository;

import com.example.portpilot.domain.portfolio.entity.Portfolio;
import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /** 특정 사용자의 포트폴리오 전체 조회 */
    List<Portfolio> findByUserId(Long userId);

    /** 사용자·상태별 카운트 (통계용) */
    long countByUserIdAndStatus(Long userId, PortfolioStatus status);
}
