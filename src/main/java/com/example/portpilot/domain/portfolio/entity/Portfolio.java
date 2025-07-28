package com.example.portpilot.domain.portfolio.entity;

import com.example.portpilot.domain.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="portfolios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String title;

    // 프로젝트 배경 (필수)
    @Column(columnDefinition="TEXT", nullable=false)
    private String background;

    // 프로젝트 성과
    @Column(columnDefinition="TEXT")
    private String results;

    // 핵심 기능
    @Column(columnDefinition="TEXT")
    private String features;

    // 진행 단계
    @Column(columnDefinition="TEXT")
    private String stages;

    // 프로젝트 상세
    @Column(columnDefinition="TEXT")
    private String details;

    // 추가 설명 (기존 description)
    @Column(columnDefinition="TEXT", nullable=false)
    private String description;

    // 외부 링크
    private String link;

    // 태그 (콤마 구분)
    private String tags;

    // 카테고리
    private String category;

    // 이미지 파일명 리스트 (콤마 구분)
    private String images;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private PortfolioStatus status;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}