package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "Text")
    private String content;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int viewCount =0;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "tech_stack")
    private String techStack;

    private String imageUrl;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

}
