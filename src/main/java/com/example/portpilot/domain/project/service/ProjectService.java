// src/main/java/com/example/portpilot/domain/project/service/ProjectService.java
package com.example.portpilot.domain.project.service;

import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;
import com.example.portpilot.domain.project.repository.ParticipationRepository;
import com.example.portpilot.domain.project.repository.ProjectRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final ParticipationRepository partRepo;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepo,
                          ParticipationRepository partRepo,
                          UserService userService) {
        this.projectRepo = projectRepo;
        this.partRepo = partRepo;
        this.userService = userService;
    }

    /** OPEN 상태인 프로젝트 전체 조회 */
    @Transactional(readOnly = true)
    public List<Project> findAllOpen() {
        return projectRepo.findByStatus(ProjectStatus.OPEN);
    }

    /** 프로젝트 생성 */
    public Project createProject(Long ownerId, String title, String description) {
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + ownerId));

        Project p = new Project();
        p.setOwner(owner);
        p.setStatus(ProjectStatus.OPEN);
        p.setTitle(title);
        p.setDescription(description);
        return projectRepo.save(p);
    }

    /** 프로젝트 참여 */
    public void joinProject(Long projectId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId));

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. id=" + projectId));

        if (partRepo.existsByUserIdAndProjectId(userId, projectId)) {
            throw new IllegalStateException("이미 참여한 프로젝트입니다.");
        }

        Participation participation = new Participation();
        participation.setUser(user);
        participation.setProject(project);
        partRepo.save(participation);
    }

    /** 사용자가 참여한 프로젝트 리스트 */
    @Transactional(readOnly = true)
    public List<Project> getProjectsForUser(Long userId) {
        return partRepo.findByUserId(userId).stream()
                .map(Participation::getProject)
                .collect(Collectors.toList());
    }

    /** 주어진 소유자·상태의 프로젝트 개수 */
    @Transactional(readOnly = true)
    public long countByOwnerAndStatus(Long ownerId, ProjectStatus status) {
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + ownerId));

        return projectRepo.countByOwnerAndStatus(owner, status);
    }

    /** 전체 프로젝트 개수 조회 (테스트용) */
    @Transactional(readOnly = true)
    public long countTotalProjects() {
        return projectRepo.count();
    }

    // =================================================
    // 아래는 새로 추가된 오버로드 메서드입니다.
    // title, description 외에 모든 옵션 필드를 받아 함께 저장합니다.
    // =================================================

    /**
     * 프로젝트 생성 (추가 필드 포함)
     */
    public Project createProject(
            Long ownerId,
            String title,
            String description,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            StartOption startOption,
            ProjectType projectType,
            PlanningState planningState,
            Experience experience,
            CollaborationOption collaborationOption) {

        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + ownerId));

        Project p = new Project();
        p.setOwner(owner);
        p.setStatus(ProjectStatus.OPEN);
        p.setTitle(title);
        p.setDescription(description);

        // 신규 필드 세팅
        p.setDeadline(deadline);
        p.setStartOption(startOption);
        p.setProjectType(projectType);
        p.setPlanningState(planningState);
        p.setExperience(experience);
        p.setCollaborationOption(collaborationOption);

        return projectRepo.save(p);
    }
    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. id=" + id));
    }

    /**
     * 프로젝트 삭제 (소유자만)
     */
    public void deleteProject(Long projectId, Long ownerId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. id=" + projectId));

        if (!project.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("본인이 소유한 프로젝트만 삭제할 수 있습니다.");
        }

        projectRepo.delete(project);
    }

    /** 내가 소유한 모든 프로젝트 조회 */
    @Transactional(readOnly = true)
    public List<Project> findByOwner(Long ownerId) {
        return projectRepo.findByOwnerId(ownerId);
    }

    /** 내가 참여한 모든 프로젝트 조회 */
    @Transactional(readOnly = true)
    public List<Project> findByParticipation(Long userId) {
        return partRepo.findByUserId(userId).stream()
                .map(Participation::getProject)
                .collect(Collectors.toList());
    }
}
