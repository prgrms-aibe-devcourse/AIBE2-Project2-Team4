package com.example.portpilot.domain.mentorRequest.entity;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.global.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "mentor_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 멘토인 사용자
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", unique = true)
    private User user;

    // 전문 기술 스택
    @Column(nullable = false)
    private String techStack;

    // 소개글
    @Column(length = 1000)
    private String description;
}
