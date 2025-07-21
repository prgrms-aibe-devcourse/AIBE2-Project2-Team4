package com.example.portpilot.adminPage.admin;

import com.example.portpilot.global.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Entity
@Table(name = "t_admin")
@Getter @Setter
@ToString
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "admin_id")
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    private Boolean isActive = true; // 기본값 true

    // Admin 생성 메서드
    public static Admin createAdmin(AdminFormDto dto, PasswordEncoder passwordEncoder) {
        Admin admin = new Admin();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setAddress(dto.getAddress());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setIsActive(true);
        return admin;
    }
}
