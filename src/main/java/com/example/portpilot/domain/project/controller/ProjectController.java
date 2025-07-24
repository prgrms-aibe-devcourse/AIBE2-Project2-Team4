package com.example.portpilot.domain.project.controller;

import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.user.UserPrincipal;
import com.example.portpilot.domain.user.User;             // <— User 엔티티 import
import com.example.portpilot.domain.user.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 등록, 리스트, 상세, 참여, 삭제, 관리 기능을 제공하는 컨트롤러
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectController(ProjectService projectService,
                             UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    /**
     * 모든 뷰에서 사용할 로그인 사용자 정보
     * Authentication 에서 꺼낸 principal 타입을 판별해 UserPrincipal 로 반환
     */
    @ModelAttribute("principal")
    public UserPrincipal principal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object raw = authentication.getPrincipal();
        // 이미 UserPrincipal 인 경우 그대로 반환
        if (raw instanceof UserPrincipal) {
            return (UserPrincipal) raw;
        }

        // 스프링 시큐리티 기본 User 타입인 경우, email 으로 엔티티 조회 후 래핑
        if (raw instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) raw).getUsername();
            // UserRepository.findByEmail이 Optional이 아니라 User를 바로 반환할 때
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("사용자 정보가 없습니다: " + email);
            }
            return new UserPrincipal(user);
        }

        return null;
    }

    /**
     * 모든 뷰에서 사용할 내 오픈 프로젝트 개수
     */
    @ModelAttribute("openCount")
    public long openCount(Authentication authentication) {
        UserPrincipal principal = principal(authentication);
        if (principal == null) {
            return 0L;
        }
        return projectService.countByOwnerAndStatus(principal.getId(), ProjectStatus.OPEN);
    }

    /** 프로젝트 리스트 (OPEN 상태 전체) */
    @GetMapping
    public String listProjects(Model model) {
        List<Project> projects = projectService.findAllOpen();
        model.addAttribute("projects", projects);
        return "projects/list";
    }

    /** 프로젝트 상세 보기 */
    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
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
        UserPrincipal principal = principal(authentication);
        if (principal == null) {
            return "redirect:/users/login";
        }
        projectService.createProject(
                principal.getId(),
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

    /** 프로젝트 참여 처리 */
    @PostMapping("/join/{projectId}")
    public String joinProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        UserPrincipal principal = principal(authentication);
        if (principal != null) {
            projectService.joinProject(projectId, principal.getId());
        }
        return "redirect:/projects";
    }

    /** 프로젝트 삭제 처리 */
    @PostMapping("/delete/{id}")
    public String deleteProject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipal principal = principal(authentication);
        if (principal == null) {
            return "redirect:/users/login";
        }
        projectService.deleteProject(id, principal.getId());
        return "redirect:/projects";
    }

    /** 내 프로젝트 관리 페이지 */
    @GetMapping("/manage")
    public String manageProjects(
            Model model,
            Authentication authentication
    ) {
        UserPrincipal principal = principal(authentication);
        if (principal == null) {
            return "redirect:/users/login";
        }
        List<Project> myProjects = projectService.findByOwner(principal.getId());
        model.addAttribute("myProjects", myProjects);
        return "projects/manage";
    }

    /** 테스트용: 전체 프로젝트 개수 확인 */
    @GetMapping("/count")
    @ResponseBody
    public String projectCount() {
        long cnt = projectService.countTotalProjects();
        return "총 프로젝트 수: " + cnt;
    }
}
