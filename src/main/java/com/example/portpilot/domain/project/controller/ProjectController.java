// src/main/java/com/example/portpilot/domain/project/controller/ProjectController.java
package com.example.portpilot.domain.project.controller;

import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.project.repository.ParticipationRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserPrincipal;
import com.example.portpilot.domain.user.UserRepository;
import com.example.portpilot.domain.project.entity.ParticipationStatus;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;

    public ProjectController(ProjectService projectService,
                             UserRepository userRepository,
                             ParticipationRepository participationRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
    }

    /** 현재 로그인된 UserPrincipal 조회 헬퍼 */
    private UserPrincipal toPrincipal(Object raw) {
        if (raw instanceof UserPrincipal) {
            return (UserPrincipal) raw;
        }
        if (raw instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) raw).getUsername();
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("사용자 정보가 없습니다: " + email);
            }
            return new UserPrincipal(user);
        }
        return null;
    }

    /** 모든 뷰에서 model에 principal 추가 */
    @ModelAttribute("principal")
    public UserPrincipal principal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return toPrincipal(authentication.getPrincipal());
    }

    /** 모든 뷰에서 model에 openCount 추가 */
    @ModelAttribute("openCount")
    public long openCount(Authentication authentication) {
        UserPrincipal p = principal(authentication);
        if (p == null) return 0L;
        return projectService.countByOwnerAndStatus(p.getId(), ProjectStatus.OPEN);
    }

    /** 프로젝트 리스트 (OPEN 상태) */
    @GetMapping
    public String listProjects(Model model, Authentication authentication) {
        List<Project> projects = projectService.findAllOpen();
        model.addAttribute("projects", projects);

        UserPrincipal p = principal(authentication);
        Set<Long> requested = p == null ? Set.of() :
                projects.stream()
                        .filter(pr -> projectService.isRequested(pr.getId(), p.getId()))
                        .map(Project::getId)
                        .collect(Collectors.toSet());
        Set<Long> members = p == null ? Set.of() :
                projects.stream()
                        .filter(pr -> projectService.isMember(pr.getId(), p.getId()))
                        .map(Project::getId)
                        .collect(Collectors.toSet());

        model.addAttribute("requestedIds", requested);
        model.addAttribute("memberIds", members);
        return "projects/list";
    }

    /** 프로젝트 상세 보기 */
    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);

        UserPrincipal p = principal(authentication);
        boolean requested = p != null && projectService.isRequested(id, p.getId());
        boolean member    = p != null && projectService.isMember(id, p.getId());
        model.addAttribute("requested", requested);
        model.addAttribute("member", member);

        return "projects/detail";
    }

    /** 프로젝트 등록 폼 */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "projects/register";
    }

    /** 프로젝트 등록 처리 */
    @PostMapping("/register")
    public String registerProject(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            @RequestParam StartOption startOption,
            @RequestParam ProjectType projectType,
            @RequestParam PlanningState planningState,
            @RequestParam Experience experience,
            @RequestParam CollaborationOption collaborationOption,
            Authentication authentication
    ) {
        UserPrincipal p = principal(authentication);
        if (p == null) {
            return "redirect:/users/login";
        }
        projectService.createProject(
                p.getId(),
                title,
                description,
                deadline,
                startOption,
                projectType,
                planningState,
                experience,
                collaborationOption
        );
        return "redirect:/projects";
    }

    /** 참여 요청 처리 */
    @PostMapping("/join/{projectId}")
    public String requestParticipation(@PathVariable Long projectId,
                                       Authentication authentication,
                                       RedirectAttributes ra) {
        UserPrincipal p = principal(authentication);
        if (p == null) {
            return "redirect:/users/login";
        }
        if (projectService.isRequested(projectId, p.getId()) ||
                projectService.isMember(projectId, p.getId())) {
            ra.addFlashAttribute("error", "이미 요청했거나 참여 중입니다.");
        } else {
            projectService.requestParticipation(projectId, p.getId());
            ra.addFlashAttribute("message", "참여 요청이 정상적으로 전송되었습니다.");
        }
        return "redirect:/projects/" + projectId;
    }

    /** 프로젝트 삭제 처리 */
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, Authentication authentication) {
        UserPrincipal p = principal(authentication);
        if (p == null) {
            return "redirect:/users/login";
        }
        projectService.deleteProject(id, p.getId());
        return "redirect:/projects";
    }

    /** 내 프로젝트 관리 페이지 */
    @GetMapping("/manage")
    public String manageProjects(Model model, Authentication authentication) {
        UserPrincipal p = principal(authentication);
        if (p == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("myProjects", projectService.findByOwner(p.getId()));
        return "projects/manage";
    }

    /** 소유자: 요청 목록 */
    @GetMapping("/manage/requests")
    public String viewRequests(Model model, Authentication authentication) {
        UserPrincipal p = principal(authentication);
        if (p == null) {
            return "redirect:/users/login";
        }
        List<Participation> requests = projectService.getPendingRequests(p.getId());
        model.addAttribute("requests", requests);
        return "projects/requests";
    }

    /** 소유자: 요청 승인 */
    @PostMapping("/manage/approve/{requestId}")
    public String approve(@PathVariable Long requestId, Authentication authentication) {
        UserPrincipal p = principal(authentication);
        if (p != null) {
            projectService.approveParticipation(requestId, p.getId());
        }
        return "redirect:/projects/manage/requests";
    }

    /** 소유자: 요청 거절 */
    @PostMapping("/manage/reject/{requestId}")
    public String reject(@PathVariable Long requestId, Authentication authentication) {
        UserPrincipal p = principal(authentication);
        if (p != null) {
            projectService.rejectParticipation(requestId, p.getId());
        }
        return "redirect:/projects/manage/requests";
    }

    /** 테스트: 전체 개수 확인 */
    @GetMapping("/count")
    @ResponseBody
    public String projectCount() {
        return "총 프로젝트 수: " + projectService.countTotalProjects();
    }
}
