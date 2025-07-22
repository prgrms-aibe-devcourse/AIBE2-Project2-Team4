package com.example.portpilot.domain.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    long countByUserIdAndStatus(Long userId, PortfolioStatus status);
}
