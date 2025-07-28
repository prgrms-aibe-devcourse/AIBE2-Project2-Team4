package com.example.portpilot.domain.profile.controller;

import com.example.portpilot.domain.notification.Notification;
import com.example.portpilot.domain.notification.NotificationService;
import com.example.portpilot.domain.profile.service.ProfileService;
import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.entity.ParticipationStatus;
import com.example.portpilot.domain.project.entity.ProjectStatus;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserPrincipal;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileService      profileService;
    private final ProjectService      projectService;
    private final NotificationService notificationService;
    private final UserRepository      userRepository;

    private UserPrincipal toPrincipal(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object p = auth.getPrincipal();
        if (p instanceof UserPrincipal) {
            return (UserPrincipal) p;
        }
        if (p instanceof UserDetails) {
            String email = ((UserDetails) p).getUsername();
            User u = userRepository.findByEmail(email);
            if (u == null) throw new IllegalArgumentException("No user: " + email);
            return new UserPrincipal(u);
        }
        return null;
    }

    /** 1) 내 프로필 */
    @GetMapping("/profile")
    public String profilePage(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        Long userId = p.getId();

        model.addAttribute("profile",       profileService.getProfile());
        model.addAttribute("activityList",  profileService.getRecentActivity(5));
        model.addAttribute("stats",         profileService.getStats());
        model.addAttribute("myProjects",    projectService.getProjectsForUser(userId));
        model.addAttribute("portfolios",    profileService.getMyPortfolios(userId));
        model.addAttribute("notifications", notificationService.getNotifications(userId));

        model.addAttribute("pageTitle", "내 프로필");
        model.addAttribute("active",    "profile");
        return "profile/profile";
    }

    /** 2) 내가 보낸 요청 */
    @GetMapping("/profile/requests/sent")
    public String sentRequests(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        Long userId = p.getId();

        model.addAttribute("profile",      profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats",        profileService.getStats());
        model.addAttribute("myProjects",   projectService.getProjectsForUser(userId));

        model.addAttribute("pageTitle",    "내가 보낸 요청");
        model.addAttribute("active",       "sentRequests");
        model.addAttribute("sentRequests", profileService.getSentParticipations(userId));
        return "profile/profile";
    }

    /** 3) 받은 요청 */
    @GetMapping("/profile/requests/received")
    public String receivedRequests(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        Long userId = p.getId();

        model.addAttribute("profile",      profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats",        profileService.getStats());
        model.addAttribute("myProjects",   projectService.getProjectsForUser(userId));

        model.addAttribute("pageTitle",        "받은 요청");
        model.addAttribute("active",           "receivedRequests");
        model.addAttribute("receivedRequests", projectService.getPendingRequests(userId));
        return "profile/profile";
    }

    /** 4) 내 포트폴리오 */
    @GetMapping("/profile/portfolio")
    public String portfolio(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        Long userId = p.getId();

        model.addAttribute("profile",      profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats",        profileService.getStats());
        model.addAttribute("myProjects",   projectService.getProjectsForUser(userId));

        model.addAttribute("pageTitle",   "내 포트폴리오");
        model.addAttribute("active",      "portfolio");
        model.addAttribute("portfolios",  profileService.getMyPortfolios(userId));
        return "profile/profile";
    }

    /** 5) 포트폴리오 삭제 */
    @PostMapping("/profile/portfolio/delete/{id}")
    public String deletePortfolio(Authentication auth,
                                  @PathVariable Long id,
                                  RedirectAttributes rt) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        profileService.deletePortfolio(p.getId(), id);
        rt.addFlashAttribute("msg", "포트폴리오가 삭제되었습니다.");
        return "redirect:/profile/portfolio";
    }

    /** 6) 프로필 수정 폼 */
    @GetMapping("/profile/profileedit")
    public String editProfile(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        model.addAttribute("pageTitle", "프로필 수정");
        model.addAttribute("active",    "profile");
        model.addAttribute("profile",   profileService.getProfile());
        model.addAttribute("skills",    profileService.getSkills());
        return "profile/profileedit";
    }

    /** 7) 프로필 수정 처리 */
    @PostMapping("/profile/edit")
    public String updateProfile(Authentication auth,
                                @RequestParam String position,
                                @RequestParam String bio,
                                @RequestParam(name = "skills", required = false) List<String> skills) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        profileService.updateProfile(p.getId(), position, bio, skills);
        return "redirect:/profile";
    }

    /** 8) 나의 알림 보기 */
    @GetMapping("/profile/notifications")
    public String notifications(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        Long userId = p.getId();

        model.addAttribute("profile",      profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats",        profileService.getStats());
        model.addAttribute("myProjects",   projectService.getProjectsForUser(userId));
        model.addAttribute("notifications", notificationService.getNotifications(userId));

        model.addAttribute("pageTitle", "나의 알림");
        model.addAttribute("active",    "notifications");
        return "profile/profile";
    }

    /** 9) 프로젝트 전체 관리 */
    @GetMapping("/profile/projects")
    public String manageOwnProjects(Authentication auth, Model model) {
        UserPrincipal p = toPrincipal(auth);
        if (p == null) return "redirect:/users/login";
        Long userId = p.getId();

        model.addAttribute("profile",       profileService.getProfile());
        model.addAttribute("activityList",  profileService.getRecentActivity(5));
        model.addAttribute("stats",         profileService.getStats());
        model.addAttribute("notifications", notificationService.getNotifications(userId));

        List<Project> owned = projectService.findByOwner(userId);
        model.addAttribute("ownedProjects", owned);

        List<Project> all = projectService.getProjectsForUser(userId);
        List<Project> joined = new ArrayList<>(all);
        joined.removeAll(owned);
        model.addAttribute("joinedProjects", joined);

        model.addAttribute("pageTitle", "내 프로젝트");
        model.addAttribute("active",    "projects");
        return "profile/profile";
    }

    /** 10) 프로젝트 상태 변경 처리 */
    @PostMapping("/profile/projects/{projectId}/status")
    public String changeStatus(@PathVariable Long projectId,
                               @RequestParam ProjectStatus status,
                               Authentication auth) {
        UserPrincipal p = toPrincipal(auth);
        if (p != null) {
            projectService.changeStatus(projectId, p.getId(), status);
        }
        return "redirect:/profile/projects";
    }

    /** 11) 팀원 상태 변경 (승인/제외) */
    @PostMapping("/profile/projects/{projectId}/participant/{partId}/status")
    public String changeParticipantStatus(@PathVariable Long projectId,
                                          @PathVariable Long partId,
                                          @RequestParam ParticipationStatus status,
                                          Authentication auth) {
        UserPrincipal p = toPrincipal(auth);
        if (p != null) {
            if (status == ParticipationStatus.APPROVED) {
                projectService.approveParticipation(partId, p.getId());
            } else if (status == ParticipationStatus.REJECTED) {
                projectService.rejectParticipation(partId, p.getId());
            }
        }
        return "redirect:/profile/projects";
    }
}
