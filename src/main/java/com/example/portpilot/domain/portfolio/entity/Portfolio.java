package com.example.portpilot.domain.portfolio.entity;

import com.example.portpilot.domain.user.User;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="portfolios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Portfolio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String title;

    @Column(columnDefinition="TEXT", nullable=false)
    private String description;

    // 외부 링크 (단일 혹은 복수라면 List<String> 으로 매핑)
    private String link;

    // 태그를 콤마로 구분한 문자열로 저장 (간단 구현)
    private String tags;  // e.g. "Java,Spring,Thymeleaf"

    // 카테고리 (enum으로 두어도 좋습니다)
    private String category;

    // 이미지 파일명 또는 URL을 콤마로 구분해 저장
    private String images; // e.g. "uuid1.jpg,uuid2.png"

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private PortfolioStatus status;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;
}