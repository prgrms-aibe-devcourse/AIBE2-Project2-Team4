// src/main/java/com/example/portpilot/domain/portfolio/PortfolioRepository.java
package com.example.portpilot.domain.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.portpilot.domain.user.User;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // 작업물 도착 건수
    long countDeliveredByUser(Long userId);

    // 문제 발생/취소 건수
    long countIssuesByUser(Long userId);

    // 구매 확정 건수
    long countPurchased(Long userId);

    // 작성 가능한 리뷰 건수
    long countPendingReviews(Long userId);

    // 주문 취소 건수
    long countCancelled(Long userId);
}

