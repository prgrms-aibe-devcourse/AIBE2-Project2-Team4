package com.example.portpilot.domain.user;

import com.example.portpilot.adminPage.dashboard.SignupStatDto;
import com.example.portpilot.adminPage.dashboard.WithdrawStatDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Page<User> findByNameContainingOrEmailContaining(String name, String email, Pageable pageable);
    List<User> findAllByIsBlockedTrueAndBlockedUntilBefore(LocalDateTime time);

    @Query("SELECT new com.example.portpilot.adminPage.dashboard.SignupStatDto(FUNCTION('DATE', u.createdAt), COUNT(u)) " +
            "FROM User u WHERE u.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY FUNCTION('DATE', u.createdAt)")
    List<SignupStatDto> countNewUsersByDate(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);


    @Query("SELECT new com.example.portpilot.adminPage.dashboard.WithdrawStatDto(FUNCTION('DATE', u.deletedAt), COUNT(u)) " +
            "FROM User u WHERE u.isDeleted = true AND u.deletedAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', u.deletedAt) ORDER BY FUNCTION('DATE', u.deletedAt)")
    List<WithdrawStatDto> countWithdrawnUsersByDate(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

}