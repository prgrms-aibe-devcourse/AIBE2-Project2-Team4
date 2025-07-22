package com.example.portpilot.domain.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileViewController {
    private final ProfileService profileService;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats", profileService.getStats());
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
