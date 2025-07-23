package com.example.portpilot.domain.profile.repository;

import com.example.portpilot.domain.profile.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUserIdOrderByDateDesc(Long userId);
}

