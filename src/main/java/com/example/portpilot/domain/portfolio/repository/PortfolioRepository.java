package com.example.portpilot.domain.portfolio.repository;

import com.example.portpilot.domain.portfolio.entity.Portfolio;
import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndStatus(Long userId, PortfolioStatus status);

    List<Portfolio> findByUserId(Long userId);

    Page<Portfolio> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}