package com.example.portpilot.domain.project;

import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    long countByOwnerAndStatus(User owner, ProjectStatus status);

}
