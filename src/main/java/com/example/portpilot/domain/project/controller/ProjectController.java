package com.example.portpilot.domain.project.controller;

import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.entity.enums.StartOption;
import com.example.portpilot.domain.project.entity.enums.ProjectType;
import com.example.portpilot.domain.project.entity.enums.PlanningState;
import com.example.portpilot.domain.project.entity.enums.Experience;
import com.example.portpilot.domain.project.entity.enums.CollaborationOption;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.user.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 프로젝트 등록, 리스트, 상세, 참여 기능을 제공하는 컨트롤러
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** 프로젝트 리스트 (OPEN 상태 전체) */
    @GetMapping
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.findAllOpen());
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

    ///** 프로젝트 등록 처리 */
    //@PostMapping("/register")
    //public String registerProject(@RequestParam String title,
    //                              @RequestParam String description,
    //                              @AuthenticationPrincipal UserPrincipal principal) {
    //    if (principal != null) {
    //        projectService.createProject(principal.getId(), title, description);
    //    }
    //    return "redirect:/projects";
    //}

    /** 프로젝트 등록 처리 (확장된 파라미터) */
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
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 실제 principal 사용, 테스트할 땐 null 체크 후 하드코딩 가능
        Long ownerId = (principal != null) ? principal.getId() : 1L;
        projectService.createProject(
                ownerId,
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
    public String joinProject(@PathVariable Long projectId,
                              @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            projectService.joinProject(projectId, principal.getId());
        }
        return "redirect:/projects";
    }

    /** 내 오픈 프로젝트 개수 (모든 뷰에서 사용 가능) */
    @ModelAttribute("openCount")
    public long openCount(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return 0L;
        }
        return projectService.countByOwnerAndStatus(
                principal.getId(),
                ProjectStatus.OPEN
        );
    }

    /** 테스트용: 전체 프로젝트 개수 확인 */
    @GetMapping("/count")
    @ResponseBody
    public String projectCount() {
        long cnt = projectService.countTotalProjects();
        return "총 프로젝트 수: " + cnt;
    }
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable("id") Long id,
                                @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            // 비로그인 상태면 거부하거나 로그인 페이지로
            return "redirect:/users/login";
        }
        projectService.deleteProject(id, principal.getId());
        return "redirect:/projects";
    }
    @ModelAttribute("principal")
    public UserPrincipal principal(@AuthenticationPrincipal UserPrincipal principal) {
        return principal;
    }
}
