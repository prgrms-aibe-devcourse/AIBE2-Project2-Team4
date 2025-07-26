package com.example.portpilot.domain.user;

import com.example.portpilot.global.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.portpilot.domain.project.entity.Participation;

import java.util.Set;
import java.util.HashSet;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="t_user")
@Getter @Setter
@ToString
public class User extends BaseEntity {
    @Id
    @Column(name="user_id")

    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String address;
    @Enumerated(EnumType.STRING)

    private Role role;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private boolean isBlocked = false;

    private LocalDateTime blockedAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime blockedUntil;

    // User 엔터티 생성 메소드.
    public static User createUser(UserFormDto userFormDto, PasswordEncoder passwordEncoder){
        User user = new User();
        user.setName(userFormDto.getName());
        user.setEmail(userFormDto.getEmail());
        user.setAddress(userFormDto.getAddress());
        String password = passwordEncoder.encode(userFormDto.getPassword()); // 비밀번호 암호화하여 저장
        user.setPassword(password);
        user.setRole(Role.USER);
        return user;
    }

    // 탈퇴 처리
    public void withdraw() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 차단 처리
    public void blockUntil(LocalDateTime until) {
        this.isBlocked = true;
        this.blockedAt = LocalDateTime.now();
        this.blockedUntil = until;
    }

    // 차단 해제
    public void unblock() {
        this.isBlocked = false;
        this.blockedAt = null;
        this.blockedUntil = null;
    }
    //참여 매핑 필드 선언
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Participation> participations = new HashSet<>();

    //편의 메서드 (양방향 연관관계 동기화)
    public void addParticipation(Participation p) {
        this.participations.add(p);
        p.setUser(this);
    }
    public void removeParticipation(Participation p) {
        this.participations.remove(p);
        p.setUser(null);
    }

}