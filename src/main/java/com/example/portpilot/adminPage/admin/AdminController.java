package com.example.portpilot.adminPage.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/new")
    public String adminForm(Model model) {
        model.addAttribute("adminFormDto", new AdminFormDto());
        return "admin/adminForm";
    }

    @PostMapping("/new")
    public String newAdmin(@Valid AdminFormDto adminFormDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/adminForm";
        }

        try {
            Admin admin = Admin.createAdmin(adminFormDto, passwordEncoder);
            adminService.saveAdmin(admin);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/adminForm";
        }

        return "redirect:/admin";
    }

    @GetMapping("/login")
    public String loginAdmin() {
        return "admin/adminLoginForm";
    }

    @GetMapping("/login/error")
    public String loginAdminError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해 주세요");
        return "admin/adminLoginForm";
    }
}