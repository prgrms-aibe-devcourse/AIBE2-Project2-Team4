package com.example.portpilot.domain.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // ✅ 추가: 수정 폼 뷰
    @GetMapping("/profile/edit")
    public String editProfilePage(Model model) {
        ProfileDto profile = profileService.getProfile();
        List<String> skills = profileService.getSkills();  // 새로운 메서드 필요

        model.addAttribute("profile", profile);
        model.addAttribute("skills", skills);
        return "profileedit";  // -> templates/profile/profileedit.html


    }

    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam String position,
                                @RequestParam String bio) {
        profileService.updateProfile(position, bio);
        return "redirect:/profile"; // 수정 후 프로필 조회 페이지로 이동
    }



}

