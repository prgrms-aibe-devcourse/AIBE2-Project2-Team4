package com.example.portpilot.domain.profile.controller;

import com.example.portpilot.domain.profile.service.ProfileService;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 프로필 페이지 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class ProfileViewController {
    private final ProfileService profileService;
    private final ProjectService projectService;

    /**
     * 내 프로필 페이지
     */
    @GetMapping("/profile")
    public String profilePage(
            @AuthenticationPrincipal UserPrincipal principal,
            Model model
    ) {
        // 프로필 정보가 없으면 편집 페이지로 이동
        try {
            model.addAttribute("profile", profileService.getProfile());
        } catch (IllegalStateException e) {
            return "redirect:/profile/profileedit";
        }

        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats", profileService.getStats());

        // 내가 참여한 프로젝트 데이터
        Long userId = principal != null ? principal.getId() : 1L;  // 테스트 환경용 기본 ID
        List<Project> myProjects = projectService.getProjectsForUser(userId);
        model.addAttribute("myProjects", myProjects);

        return "profile/profile";
    }

    @GetMapping("/profile/profileedit")
    public String editProfilePage(Model model) {
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("skills", profileService.getSkills());
        return "profile/profileedit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            @RequestParam String position,
            @RequestParam String bio,
            @RequestParam(name = "skills", required = false) List<String> skills
    ) {
        if (skills == null) skills = Collections.emptyList();
        profileService.updateProfile(position, bio, skills);
        return "redirect:/profile";
    }
}
