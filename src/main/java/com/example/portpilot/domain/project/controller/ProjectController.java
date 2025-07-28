package com.example.portpilot.domain.project.controller;

import com.example.portpilot.domain.notification.NotificationService;
import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.project.repository.ParticipationRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserPrincipal;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final NotificationService notificationService;

    /** 현재 로그인된 UserPrincipal 조회 헬퍼 */
    private UserPrincipal toPrincipal(Object raw) {
        if (raw instanceof UserPrincipal) {
            return (UserPrincipal) raw;
        }
        if (raw instanceof UserDetails) {
            String email = ((UserDetails) raw).getUsername();
            User u = userRepository.findByEmail(email);
            if (u == null) throw new IllegalArgumentException("No user: " + email);
            return new UserPrincipal(u);
        }
        return null;
    }

    /** 모든 뷰에서 model에 principal 추가 */
    @ModelAttribute("principal")
    public UserPrincipal principal(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return toPrincipal(auth.getPrincipal());
    }

    /** 모든 뷰에서 model에 openCount 추가 */
    @ModelAttribute("openCount")
    public long openCount(Authentication auth) {
        UserPrincipal p = principal(auth);
        return (p == null) ? 0L
                : projectService.countByOwnerAndStatus(p.getId(), ProjectStatus.OPEN);
    }

    /** 1) 리스트(OPEN) */
    @GetMapping
    public String listProjects(Model model, Authentication auth) {
        List<Project> projects = projectService.findAllOpen();
        model.addAttribute("projects", projects);

        UserPrincipal p = principal(auth);
        Set<Long> requested = (p == null) ? Set.of()
                : projects.stream()
                .filter(pr -> projectService.isRequested(pr.getId(), p.getId()))
                .map(Project::getId).collect(Collectors.toSet());
        Set<Long> members = (p == null) ? Set.of()
                : projects.stream()
                .filter(pr -> projectService.isMember(pr.getId(), p.getId()))
                .map(Project::getId).collect(Collectors.toSet());

        model.addAttribute("requestedIds", requested);
        model.addAttribute("memberIds", members);
        return "projects/list";
    }

    /** 2) 상세 */
    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id, Model model, Authentication auth) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);

        UserPrincipal p = principal(auth);
        boolean requested = (p != null && projectService.isRequested(id, p.getId()));
        boolean member    = (p != null && projectService.isMember(id, p.getId()));
        boolean isOwner   = (p != null && project.getOwner().getId().equals(p.getId()));
        boolean canJoin   = (p != null && !isOwner && !member);

        model.addAttribute("requested", requested);
        model.addAttribute("member",    member);
        model.addAttribute("canJoin",   canJoin);
        return "projects/detail";
    }

    /** 3) 등록 폼 */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("startOptions",   StartOption.values());
        model.addAttribute("projectTypes",   ProjectType.values());
        model.addAttribute("planningStates", PlanningState.values());
        model.addAttribute("experiences",    Experience.values());
        model.addAttribute("collabs",        CollaborationOption.values());
        return "projects/register";
    }

    /** 4) 등록 처리 */
    @PostMapping("/register")
    public String registerProject(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime deadline,
            @RequestParam StartOption startOption,
            @RequestParam ProjectType projectType,
            @RequestParam PlanningState planningState,
            @RequestParam Experience experience,
            @RequestParam CollaborationOption collaborationOption,
            Authentication auth
    ) {
        UserPrincipal p = principal(auth);
        if (p == null) return "redirect:/users/login";
        projectService.createProject(
                p.getId(), title, description, deadline,
                startOption, projectType, planningState, experience, collaborationOption
        );
        return "redirect:/projects";
    }

    /** 5) 참여 요청 */
    @PostMapping("/join/{projectId}")
    public String requestParticipation(
            @PathVariable Long projectId,
            Authentication auth,
            RedirectAttributes ra
    ) {
        UserPrincipal p = principal(auth);
        if (p == null) return "redirect:/users/login";

        Project project = projectService.findById(projectId);
        if (project.getOwner().getId().equals(p.getId())) {
            ra.addFlashAttribute("error", "소유자는 참여할 수 없습니다.");
        } else if (projectService.isRequested(projectId, p.getId())
                || projectService.isMember(projectId, p.getId())) {
            ra.addFlashAttribute("error", "이미 요청했거나 참여 중입니다.");
        } else {
            projectService.requestParticipation(projectId, p.getId());
            ra.addFlashAttribute("message", "참여 요청을 전송했습니다.");
        }
        return "redirect:/projects/" + projectId;
    }

    /** 6) 삭제 */
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, Authentication auth) {
        UserPrincipal p = principal(auth);
        if (p == null) return "redirect:/users/login";
        projectService.deleteProject(id, p.getId());
        return "redirect:/projects";
    }

    /** 7) 내 프로젝트 관리 */
    @GetMapping("/manage")
    public String manageProjects(Model model, Authentication auth) {
        UserPrincipal p = principal(auth);
        if (p == null) return "redirect:/users/login";
        model.addAttribute("myProjects", projectService.findByOwner(p.getId()));
        return "projects/manage";
    }

    /** 8) 요청 목록 */
    @GetMapping("/manage/requests")
    public String viewRequests(Model model, Authentication auth) {
        UserPrincipal p = principal(auth);
        if (p == null) return "redirect:/users/login";
        model.addAttribute("requests", projectService.getPendingRequests(p.getId()));
        return "projects/requests";
    }

    /** 9) 승인 + 알림 */
    @PostMapping("/manage/approve/{requestId}")
    public String approve(@PathVariable Long requestId, Authentication auth) {
        UserPrincipal p = principal(auth);
        projectService.approveParticipation(requestId, p.getId());
        Participation part = participationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Participation not found"));
        notificationService.notifyProjectResult(
                part.getUser().getId(),
                part.getProject().getId(),
                true,
                part.getProject().getTitle()
        );
        return "redirect:/projects/manage/requests";
    }

    /** 10) 거절 + 알림 */
    @PostMapping("/manage/reject/{requestId}")
    public String reject(@PathVariable Long requestId, Authentication auth) {
        UserPrincipal p = principal(auth);
        projectService.rejectParticipation(requestId, p.getId());
        Participation part = participationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Participation not found"));
        notificationService.notifyProjectResult(
                part.getUser().getId(),
                part.getProject().getId(),
                false,
                part.getProject().getTitle()
        );
        return "redirect:/projects/manage/requests";
    }

    /** 11) 수정 폼 */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Authentication auth, Model model) {
        Project project = projectService.findById(id);
        UserPrincipal p = principal(auth);
        if (p == null || !project.getOwner().getId().equals(p.getId())) {
            return "redirect:/projects";
        }
        model.addAttribute("project", project);
        model.addAttribute("startOptions",   StartOption.values());
        model.addAttribute("projectTypes",   ProjectType.values());
        model.addAttribute("planningStates", PlanningState.values());
        model.addAttribute("experiences",    Experience.values());
        model.addAttribute("collabs",        CollaborationOption.values());
        return "projects/edit";
    }

    /** 12) 수정 처리 */
    @PostMapping("/edit/{id}")
    public String updateProject(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime deadline,
            @RequestParam StartOption startOption,
            @RequestParam ProjectType projectType,
            @RequestParam PlanningState planningState,
            @RequestParam Experience experience,
            @RequestParam CollaborationOption collaborationOption,
            Authentication auth
    ) {
        UserPrincipal p = principal(auth);
        projectService.updateProject(
                id, p.getId(), title, description, deadline,
                startOption, projectType, planningState, experience, collaborationOption
        );
        return "redirect:/projects/" + id;
    }

    /** 13) 카운트 테스트 */
    @GetMapping("/count")
    @ResponseBody
    public String projectCount() {
        return "총 프로젝트 수: " + projectService.countTotalProjects();
    }
}