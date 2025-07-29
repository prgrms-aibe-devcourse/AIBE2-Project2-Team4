package com.example.portpilot.global.controller;

import com.example.portpilot.domain.job.entity.JobPosition;
import com.example.portpilot.domain.job.service.JobPositionService;
import com.example.portpilot.domain.project.entity.Project;
import com.example.portpilot.domain.project.service.ProjectService;
import com.example.portpilot.domain.study.entity.StudyRecruitment;
import com.example.portpilot.domain.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProjectService projectService;
    private final StudyService studyService;
    private final JobPositionService jobPositionService;

    @GetMapping("/")
    public String main(Model model) {
        // 모집 중인 프로젝트
        List<Project> projects = projectService.findLatestOpenProjects(4);
        model.addAttribute("projects", projects);

        // 모집 중인 스터디
        List<StudyRecruitment> studies = studyService.findLatestOpenStudies(4);
        model.addAttribute("studies", studies);

        //모집 중인 채용 공고 (추가?)

        return "main";
    }
}
