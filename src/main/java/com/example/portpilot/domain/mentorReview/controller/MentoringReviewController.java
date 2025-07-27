package com.example.portpilot.domain.mentorReview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mentoringReview")
public class MentoringReviewController {

    // 멘토링 후기 목록 페이지
    @GetMapping
    public String showReviewListPage() {
        return "mentoringReview/mentoringReviewList";
    }

    // 멘토링 후기 작성 페이지
    @GetMapping("/write")
    public String showReviewWritePage() {
        return "mentoringReview/mentoringReviewWrite";
    }

    // 멘토링 후기 상세보기 페이지
    @GetMapping("/detail")
    public String showReviewDetailPage() {
        return "mentoringReview/mentoringReviewDetail";
    }

    @GetMapping("/edit")
    public String showReviewEditPage() {
        return "mentoringReview/mentoringReviewEdit";
    }
}