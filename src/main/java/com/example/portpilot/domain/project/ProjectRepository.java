// src/main/java/com/example/portpilot/domain/project/ProjectRepository.java
package com.example.portpilot.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.portpilot.domain.user.User;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 프로필 카드용 “진행 중” 프로젝트 수 집계
    long countByOwnerAndStatus(User owner, ProjectStatus status);
}
