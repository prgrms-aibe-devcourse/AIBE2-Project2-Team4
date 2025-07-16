package com.example.portpilot.domain.user;

import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

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
}