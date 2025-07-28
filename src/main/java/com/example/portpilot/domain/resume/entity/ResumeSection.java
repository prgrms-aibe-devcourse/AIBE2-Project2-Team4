package com.example.portpilot.domain.resume.entity;

import com.example.portpilot.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "resume_section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeSection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @JsonIgnore
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private SectionType sectionType;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "word_count")
    private Integer wordCount;

    @Builder
    public ResumeSection(Resume resume, SectionType sectionType, String content, Integer wordCount) {
        this.resume = resume;
        this.sectionType = sectionType;
        this.content = content;
        this.wordCount = wordCount;
    }

    public void updateContent(String content) {
        this.content = content;
        this.wordCount = content != null ? content.length() : 0;
    }
}
