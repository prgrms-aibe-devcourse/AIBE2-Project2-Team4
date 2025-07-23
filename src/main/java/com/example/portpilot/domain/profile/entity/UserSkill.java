package com.example.portpilot.domain.profile.entity;

import com.example.portpilot.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "user_skill")
@Getter @Setter @NoArgsConstructor
public class UserSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String skillName;
    private Integer level;    // Optional: 숙련도
}
