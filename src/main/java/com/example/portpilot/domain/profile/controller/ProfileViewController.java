package com.example.portpilot.domain.profile.controller;

import com.example.portpilot.domain.profile.service.ProfileService;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserPrincipal;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileService profileService;
    private final ProjectService projectService;
    private final UserRepository userRepository;

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

    @GetMapping("/profile")
    public String profilePage(Authentication auth, Model model) {
        UserPrincipal principal = toPrincipal(auth);
        if (principal == null) return "redirect:/users/login";

        Long userId = principal.getId();
        model.addAttribute("pageTitle", "내 프로필");
        model.addAttribute("active", "profile");
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats", profileService.getStats());
        model.addAttribute("myProjects", projectService.getProjectsForUser(userId));
        return "profile/profile";
    }

    @GetMapping("/profile/requests/sent")
    public String sentRequests(Authentication auth, Model model) {
        UserPrincipal principal = toPrincipal(auth);
        if (principal == null) return "redirect:/users/login";

        Long userId = principal.getId();
        model.addAttribute("pageTitle", "내가 보낸 요청");
        model.addAttribute("active", "sentRequests");
        model.addAttribute("sentRequests", profileService.getSentParticipations(userId));
        return "profile/profile";
    }

    @GetMapping("/profile/requests/received")
    public String receivedRequests(Authentication auth, Model model) {
        UserPrincipal principal = toPrincipal(auth);
        if (principal == null) return "redirect:/users/login";

        Long userId = principal.getId();
        model.addAttribute("pageTitle", "받은 요청");
        model.addAttribute("active", "receivedRequests");
        model.addAttribute("receivedRequests", projectService.getPendingRequests(userId));
        return "profile/profile";
    }

    @GetMapping("/profile/portfolio")
    public String portfolio(Authentication auth, Model model) {
        UserPrincipal principal = toPrincipal(auth);
        if (principal == null) return "redirect:/users/login";

        Long userId = principal.getId();
        model.addAttribute("pageTitle", "내 포트폴리오");
        model.addAttribute("active", "templates/portfolio");
        model.addAttribute("portfolios", profileService.getMyPortfolios(userId));
        return "profile/profile";
    }

    @GetMapping("/profile/profileedit")
    public String editProfile(Authentication auth, Model model) {
        UserPrincipal principal = toPrincipal(auth);
        if (principal == null) return "redirect:/users/login";

        model.addAttribute("pageTitle", "프로필 수정");
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("skills", profileService.getSkills());
        return "profile/profileedit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            Authentication auth,
            @RequestParam String position,
            @RequestParam String bio,
            @RequestParam(name = "skills", required = false) List<String> skills
    ) {
        UserPrincipal principal = toPrincipal(auth);
        if (principal == null) return "redirect:/users/login";

        profileService.updateProfile(principal.getId(), position, bio, skills);
        return "redirect:/profile";
    }
}