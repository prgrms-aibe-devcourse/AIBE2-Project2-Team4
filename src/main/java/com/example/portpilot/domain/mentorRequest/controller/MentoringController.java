package com.example.portpilot.domain.mentorRequest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mentoring")
public class MentoringController {

    //멘토 리스트 페이지
    @GetMapping
    public String showMentorListPage() {
        return "mentoringRequest/mentoringList"; // mentoringList.html
    }

    //멘토링 신청 페이지
    @GetMapping("/request")
    public String showMentoringRequestForm() {
        return "mentoringRequest/mentoringRequest"; // mentoringRequest.html
    }

    //멘토 등록 페이지
    @GetMapping("/register")
    public String showMentorRegisterForm() {
        return "mentoringRequest/mentorRegister"; // mentorRegister.html
    }
    // 멘토링 신청 상세보기 페이지
    @GetMapping("/detail")
    public String showMentoringDetailPage() {
        return "mentoringRequest/mentoringDetail"; // mentoringDetail.html
    }
}
