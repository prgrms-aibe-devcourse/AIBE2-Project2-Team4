package com.example.portpilot.adminPage.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public Admin saveAdmin(Admin admin) {
        validateDuplicateAdmin(admin);
        return adminRepository.save(admin);
    }

    private void validateDuplicateAdmin(Admin admin) {
        Admin findAdmin = adminRepository.findByEmail(admin.getEmail());
        if (findAdmin != null) {
            throw new IllegalStateException("이미 등록된 관리자입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(admin.getEmail())
                .password(admin.getPassword())
                .authorities("ROLE_ADMIN")
                .build();
    }

}
