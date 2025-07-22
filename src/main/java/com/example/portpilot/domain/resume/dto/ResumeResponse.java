package com.example.portpilot.domain.resume.dto;

import com.example.portpilot.domain.resume.entity.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResumeResponse {

    private final Long id;
    private final String title;
    private final String industry;
    private final String position;
    private final String targetCompany;
    private final String highlights;
    private final ResumeStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private final List<ResumeSection> sections;
    private final List<Education> educations;
    private final List<Career> careers;
    private final List<Experience> experiences;

    public ResumeResponse(Resume resume) {
        this.id = resume.getId();
        this.title = resume.getTitle();
        this.industry = resume.getIndustry();
        this.position = resume.getPosition();
        this.targetCompany = resume.getTargetCompany();
        this.highlights = resume.getHighlights();
        this.status = resume.getStatus();
        this.createdAt = resume.getCreatedAt();
        this.updatedAt = resume.getUpdatedAt();

        this.sections = resume.getSections();
        this.educations = resume.getEducations();
        this.careers = resume.getCareers();
        this.experiences = resume.getExperiences();
    }
}
