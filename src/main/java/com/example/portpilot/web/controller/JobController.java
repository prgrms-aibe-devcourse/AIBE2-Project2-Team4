package com.example.portpilot.web.controller;

import com.example.portpilot.domain.job.entity.JobPosition;
import com.example.portpilot.domain.job.service.JobPositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;  // ← 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;  // ← * 추가 (기존 GetMapping, RequestMapping 포함)
import org.springframework.web.servlet.mvc.support.RedirectAttributes;  // ← 추가

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/jobs")
@Slf4j
public class JobController {

    private final JobPositionService jobPositionService;

    /**
     * 채용공고 목록 페이지
     */
    @GetMapping
    public String jobList(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "status", required = false, defaultValue = "모집중") String status,
            Model model) {

        log.info("채용공고 목록 조회 - title: {}, location: {}, status: {}", title, location, status);

        List<JobPosition> jobPositions;

        // 검색 조건이 있으면 검색, 없으면 전체 조회
        if (title != null || location != null) {
            jobPositions = jobPositionService.searchJobs(title, location, status);
        } else {
            jobPositions = jobPositionService.getActiveJobPositions();
        }

        // 뷰에 데이터 전달
        model.addAttribute("jobPositions", jobPositions);
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchLocation", location);
        model.addAttribute("searchStatus", status);
        model.addAttribute("totalCount", jobPositions.size());

        return "jobs/list"; // templates/jobs/list.html
    }

    /**
     * 메인 페이지에서 채용공고 목록으로 리다이렉트
     */
    @GetMapping("/")
    public String redirectToJobList() {
        return "redirect:/jobs";
    }

    // 기존 JobController.java에 추가할 메서드들

    /**
     * 채용공고 상세보기
     */
    @GetMapping("/{id}")
    public String jobDetail(@PathVariable Long id, Model model) {
        log.info("채용공고 상세 조회 - ID: {}", id);

        Optional<JobPosition> jobPosition = jobPositionService.getJobPositionById(id);

        if (jobPosition.isPresent()) {
            model.addAttribute("jobPosition", jobPosition.get());
            return "jobs/detail"; // templates/jobs/detail.html
        } else {
            return "redirect:/jobs?error=notfound";
        }
    }

    /**
     * 채용공고 등록 폼 페이지
     */
    @GetMapping("/new")
    public String jobForm(Model model) {
        log.info("채용공고 등록 폼 조회");

        model.addAttribute("jobPosition", new JobPosition());
        return "jobs/form"; // templates/jobs/form.html
    }

    /**
     * 채용공고 등록 처리
     */
    @PostMapping
    public String createJob(@ModelAttribute JobPosition jobPosition,
                            RedirectAttributes redirectAttributes) {
        log.info("채용공고 등록 - 제목: {}", jobPosition.getTitle());

        try {
            // TODO: 현재 로그인한 사용자의 회사 정보 설정
            // jobPosition.setCompany(currentUserCompany);

            JobPosition savedJob = jobPositionService.createJobPosition(jobPosition);
            redirectAttributes.addFlashAttribute("message", "채용공고가 성공적으로 등록되었습니다.");
            return "redirect:/jobs/" + savedJob.getId();
        } catch (Exception e) {
            log.error("채용공고 등록 실패", e);
            redirectAttributes.addFlashAttribute("error", "채용공고 등록에 실패했습니다.");
            return "redirect:/jobs/new";
        }
    }

    /**
     * 채용공고 수정 폼 페이지
     */
    @GetMapping("/{id}/edit")
    public String editJobForm(@PathVariable Long id, Model model) {
        log.info("채용공고 수정 폼 조회 - ID: {}", id);

        Optional<JobPosition> jobPosition = jobPositionService.getJobPositionById(id);

        if (jobPosition.isPresent()) {
            model.addAttribute("jobPosition", jobPosition.get());
            return "jobs/form"; // 등록 폼과 동일한 템플릿 사용
        } else {
            return "redirect:/jobs?error=notfound";
        }
    }

    /**
     * 채용공고 수정 처리
     */
    @PostMapping("/{id}")
    public String updateJob(@PathVariable Long id,
                            @ModelAttribute JobPosition jobPosition,
                            RedirectAttributes redirectAttributes) {
        log.info("채용공고 수정 - ID: {}, 제목: {}", id, jobPosition.getTitle());

        try {
            JobPosition updatedJob = jobPositionService.updateJobPosition(id, jobPosition);
            redirectAttributes.addFlashAttribute("message", "채용공고가 성공적으로 수정되었습니다.");
            return "redirect:/jobs/" + updatedJob.getId();
        } catch (Exception e) {
            log.error("채용공고 수정 실패", e);
            redirectAttributes.addFlashAttribute("error", "채용공고 수정에 실패했습니다.");
            return "redirect:/jobs/" + id + "/edit";
        }
    }

    /**
     * 채용공고 삭제
     */
    @PostMapping("/{id}/delete")
    public String deleteJob(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        log.info("채용공고 삭제 - ID: {}", id);

        try {
            jobPositionService.deleteJobPosition(id);
            redirectAttributes.addFlashAttribute("message", "채용공고가 성공적으로 삭제되었습니다.");
            return "redirect:/jobs";
        } catch (Exception e) {
            log.error("채용공고 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "채용공고 삭제에 실패했습니다.");
            return "redirect:/jobs";
        }
    }

    /**
     * 채용공고 상태 변경 (AJAX용)
     */
    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateJobStatus(@PathVariable Long id,
                                                  @RequestParam String status) {
        try {
            jobPositionService.updateJobStatus(id, status);
            return ResponseEntity.ok("상태가 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("상태 변경에 실패했습니다.");
        }
    }
}