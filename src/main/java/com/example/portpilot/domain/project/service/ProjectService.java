package com.example.portpilot.domain.project.service;

import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ParticipationStatus;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.repository.ParticipationRepository;
import com.example.portpilot.domain.project.repository.ProjectRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.*;
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

    /** 프로젝트 상세 조회 */
    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. id=" + id));
    }

    /** 프로젝트 생성 (모든 옵션 포함) */
    public Project createProject(
            Long ownerId,
            String title,
            String description,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            StartOption startOption,
            ProjectType projectType,
            PlanningState planningState,
            Experience experience,
            CollaborationOption collaborationOption
    ) {
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + ownerId));

        Project p = new Project();
        p.setOwner(owner);
        p.setStatus(ProjectStatus.OPEN);
        p.setTitle(title);
        p.setDescription(description);
        p.setDeadline(deadline);
        p.setStartOption(startOption);
        p.setProjectType(projectType);
        p.setPlanningState(planningState);
        p.setExperience(experience);
        p.setCollaborationOption(collaborationOption);

        return projectRepo.save(p);
    }

    /** 참여 요청 (PENDING) */
    public void requestParticipation(Long projectId, Long userId) {
        if (partRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, ParticipationStatus.PENDING)
                || partRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, ParticipationStatus.APPROVED)) {
            throw new IllegalStateException("이미 요청했거나 참여 중입니다.");
        }

        Project project = findById(projectId);
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다. id=" + userId));

        Participation p = new Participation();
        p.setProject(project);
        p.setUser(user);
        p.setStatus(ParticipationStatus.PENDING);
        p.setRequestedAt(LocalDateTime.now());
        partRepo.save(p);
    }

    /** joinProject 는 requestParticipation 호출로 대체 */
    public void joinProject(Long projectId, Long userId) {
        requestParticipation(projectId, userId);
    }

    /** 요청 승인 */
    public void approveParticipation(Long requestId, Long ownerId) {
        Participation p = partRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청이 없습니다. id=" + requestId));
        if (!p.getProject().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("승인 권한이 없습니다.");
        }
        p.setStatus(ParticipationStatus.APPROVED);
    }

    /** 요청 거절 */
    public void rejectParticipation(Long requestId, Long ownerId) {
        Participation p = partRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청이 없습니다. id=" + requestId));
        if (!p.getProject().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("거절 권한이 없습니다.");
        }
        p.setStatus(ParticipationStatus.REJECTED);
    }

    /** 대기 중 요청 전체 조회 (소유자용) */
    @Transactional(readOnly = true)
    public List<Participation> getPendingRequests(Long ownerId) {
        return partRepo.findByProjectOwnerIdAndStatus(ownerId, ParticipationStatus.PENDING);
    }

    /** 이미 요청했는지 */
    @Transactional(readOnly = true)
    public boolean isRequested(Long projectId, Long userId) {
        return partRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, ParticipationStatus.PENDING);
    }

    /** 이미 참여 중인지 */
    @Transactional(readOnly = true)
    public boolean isMember(Long projectId, Long userId) {
        return partRepo.existsByProjectIdAndUserIdAndStatus(projectId, userId, ParticipationStatus.APPROVED);
    }

    /**
     * 내 프로젝트(소유 + 참여) 조회
     */
    @Transactional(readOnly = true)
    public List<Project> getProjectsForUser(Long userId) {
        // 1) 소유 프로젝트
        List<Project> owned = projectRepo.findByOwnerId(userId);
        // 2) 참가자로 승인된 프로젝트
        List<Project> joined = partRepo.findByUserIdAndStatus(userId, ParticipationStatus.APPROVED).stream()
                .map(Participation::getProject)
                .collect(Collectors.toList());
        // 3) 합치고 중복 제거 (순서 유지)
        Set<Project> merged = new LinkedHashSet<>();
        merged.addAll(owned);
        merged.addAll(joined);
        return new ArrayList<>(merged);
    }

    /** 프로젝트 삭제 (소유자만) */
    public void deleteProject(Long projectId, Long ownerId) {
        Project project = findById(projectId);
        if (!project.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("본인이 소유한 프로젝트만 삭제할 수 있습니다.");
        }
        projectRepo.delete(project);
    }

    /** 내가 만든 프로젝트 전체 조회 */
    @Transactional(readOnly = true)
    public List<Project> findByOwner(Long ownerId) {
        return projectRepo.findByOwnerId(ownerId);
    }

    /** 소유자·상태별 프로젝트 개수 */
    @Transactional(readOnly = true)
    public long countByOwnerAndStatus(Long ownerId, ProjectStatus status) {
        User owner = userService.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + ownerId));
        return projectRepo.countByOwnerAndStatus(owner, status);
    }


    /**
     * 프로젝트 수정 (소유자만 가능)
     */
    public Project updateProject(
            Long projectId,
            Long ownerId,
            String title,
            String description,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            StartOption startOption,
            ProjectType projectType,
            PlanningState planningState,
            Experience experience,
            CollaborationOption collaborationOption) {

        // 1) 프로젝트 조회
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 없습니다. id=" + projectId));

        // 2) 권한 체크
        if (!project.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        // 3) 필드 업데이트
        project.setTitle(title);
        project.setDescription(description);
        project.setDeadline(deadline);
        project.setStartOption(startOption);
        project.setProjectType(projectType);
        project.setPlanningState(planningState);
        project.setExperience(experience);
        project.setCollaborationOption(collaborationOption);

        // 4) 저장
        return projectRepo.save(project);
    }


    /** 테스트용: 전체 프로젝트 수 조회 */
    @Transactional(readOnly = true)
    public long countTotalProjects() {
        return projectRepo.count();
    }
}