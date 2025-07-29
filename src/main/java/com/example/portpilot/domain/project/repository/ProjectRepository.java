package com.example.portpilot.domain.project.repository;

import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    /** OPEN 상태인 프로젝트 검색 */
    List<Project> findByStatus(ProjectStatus status);

    /** 소유자·상태로 개수 세기 */
    long countByOwnerAndStatus(User owner, ProjectStatus status);

    /** 소유자 ID로 프로젝트 전체 조회 (메인에 사용) */
    List<Project> findByOwnerId(Long ownerId);

}