package com.example.portpilot.domain.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    // 잘 동작하는 표준 메서드 시그니처로 교체
    List<ActivityLog> findByUserIdOrderByDateDesc(Long userId);

}
