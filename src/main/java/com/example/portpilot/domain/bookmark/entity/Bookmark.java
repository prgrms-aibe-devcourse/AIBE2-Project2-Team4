package com.example.portpilot.domain.bookmark.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "bookmark")
@Getter
@Setter
@NoArgsConstructor
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private BookmarkType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // 편의 생성자
    public Bookmark(User user, BookmarkType targetType, Long targetId) {
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    // BookmarkType enum
    public enum BookmarkType {
        JOB_POSITION("채용공고"),
        USER("구직자");

        private final String description;

        BookmarkType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}