package com.example.portpilot.domain.user;

import com.example.portpilot.adminPage.userManagement.UserDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional   // 로직 중 예외 발생 시 롤백
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public User saveUser(User user) {
        validateDuplicateUser(user);
        return userRepository.save(user);
    }


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateUser(User form) {
        User user = userRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        user.setName(form.getName());
        user.setAddress(form.getAddress());
        user.setDeleted(form.isDeleted());

        if (form.getBlockedUntil() != null) {
            user.blockUntil(form.getBlockedUntil());
        } else {
            user.unblock();
        }
    }




    private void validateDuplicateUser(User user) {
        User findUser = userRepository.findByEmail(user.getEmail());
        if (findUser != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }


    public Page<UserDetailDto> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByNameContainingOrEmailContaining(keyword, keyword, pageable)
                .map(UserDetailDto::new);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException(email);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())           // 로그인 아이디
                .password(user.getPassword())        // 인코딩된 비밀번호
                .roles(user.getRole().toString())    // ROLE_ 접두어는 자동 부여
                .build();
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoUnblockExpiredUsers() {
        List<User> users = userRepository.findAllByIsBlockedTrueAndBlockedUntilBefore(LocalDateTime.now());
        for (User user : users) {
            user.unblock();
        }
    }

}
