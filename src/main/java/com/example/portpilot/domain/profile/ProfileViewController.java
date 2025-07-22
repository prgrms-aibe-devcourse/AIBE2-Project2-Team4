package com.example.portpilot.domain.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileService profileService;

    // 1️⃣ 프로필 조회 화면
    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("profile", profileService.getProfile());
        model.addAttribute("activityList", profileService.getRecentActivity(5));
        model.addAttribute("stats", profileService.getStats());
        return "profile/profile";
    }

    // 2️⃣ 프로필 수정 폼 (GET)
    @GetMapping("profile/profileedit")
    public String editProfilePage(Model model) {
        ProfileDto profile = profileService.getProfile();
        List<String> skills = profileService.getSkills(); // 이 메서드 profileService에 구현 필요

        model.addAttribute("profile", profile);
        model.addAttribute("skills", skills);
        return "profile/profileedit"; // templates/profile/profileedit.html
    }

    // 3️⃣ 프로필 수정 저장 (POST)
    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam String position,
                                @RequestParam String bio) {
        profileService.updateProfile(position, bio);
        return "redirect:/profile";
    }

    // 4️⃣ 기술 추가
    @PostMapping("/profile/skill/add")
    public String addSkill(@RequestParam String newSkill) {
        profileService.addSkill(newSkill);
        return "redirect:profile/profileedit"; // 수정 페이지로 리다이렉트
    }

    // 5️⃣ 기술 삭제
    @PostMapping("/profile/skill/delete")
    public String deleteSkill(@RequestParam String skill) {
        profileService.deleteSkill(skill);
        return "redirect:profile/profileedit"; // 수정 페이지로 리다이렉트
    }
}
