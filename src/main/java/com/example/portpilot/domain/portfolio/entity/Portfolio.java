package com.example.portpilot.domain.portfolio.entity;

import com.example.portpilot.domain.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소유 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 제목 */
    @Column(nullable = false)
    private String title;

    /** 설명 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PortfolioStatus status;

    /** 생성 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
