package com.example.portpilot.domain.project.service;

import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.repository.ParticipationRepository;
import com.example.portpilot.domain.project.repository.ProjectRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
