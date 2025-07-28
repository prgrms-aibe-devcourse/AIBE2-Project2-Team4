package com.example.portpilot.domain.search.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "saved_search")
@Getter
@Setter
@NoArgsConstructor
public class SavedSearch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;              // 저장된 검색의 이름

    @Column(columnDefinition = "TEXT")
    private String searchConditions;  // JSON 형태로 저장된 검색 조건

    @Column(nullable = false)
    private boolean isBlocked = false; // 신고 처리용

    public SavedSearch(User user, String name, String searchConditions) {
        this.user = user;
        this.name = name;
        this.searchConditions = searchConditions;
    }
}