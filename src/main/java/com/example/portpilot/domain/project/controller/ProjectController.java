package com.example.portpilot.domain.project.controller;

import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.user.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 프로젝트 등록, 리스트, 참여 기능을 제공하는 컨트롤러
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

    /** 프로젝트 등록 폼 */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "projects/register";
    }

    /** 프로젝트 등록 처리 (테스트용: principal 무시, userId=1L) */
    @PostMapping("/register")
    public String registerProject(@RequestParam String title,
                                  @RequestParam String description,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        System.out.println(">>> bypass principal, 저장 테스트");
        // 테스트용 하드코딩
        projectService.createProject(1L, title, description);
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
