package com.example.portpilot.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // long countByOwnerAndStatus(User owner, ProjectStatus status);
}
