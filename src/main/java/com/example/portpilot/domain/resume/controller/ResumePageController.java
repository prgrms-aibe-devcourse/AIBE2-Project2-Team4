package com.example.portpilot.domain.resume.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumePageController {

    // 자소서 목록 페이지
    @GetMapping("/resumes")
    public String resumeListPage() {
        return "resume/resume-list";
    }

    // 정보 입력 페이지 (지금 만든 HTML)
    @GetMapping("/resumes/create")
    public String createResumePage() {
        return "resume/resume-info";
    }

    // Gemini 결과 페이지
    @GetMapping("/resumes/result")
    public String geminiResultPage(@RequestParam Long resumeId, Model model) {
        model.addAttribute("resumeId", resumeId);
        return "resume/resume-result";
    }
}
