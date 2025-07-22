// src/main/java/com/example/portpilot/domain/portfolio/PortfolioRepository.java
package com.example.portpilot.domain.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // TODO: 실제 엔티티 필드(userId, delivered, issues 등)를 정의한 뒤에 수정
    // long countDeliveredByUser(Long userId);
    // long countIssuesByUser(Long userId);
    // long countPurchased(Long userId);
    // long countPendingReviews(Long userId);
    // long countCancelled(Long userId);
}

