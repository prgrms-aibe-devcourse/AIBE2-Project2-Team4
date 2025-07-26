package com.example.portpilot.domain.resume.service;

import com.example.portpilot.domain.resume.dto.*;
import com.example.portpilot.domain.resume.entity.*;
import com.example.portpilot.domain.resume.repository.*;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeSectionRepository resumeSectionRepository;
    private final EducationRepository educationRepository;
    private final CareerRepository careerRepository;
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    // 목록 조회
    public List<ResumeResponse> getResumeList(Long userId) {
        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return resumes.stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        initializeLazyCollections(resume);

        return new ResumeResponse(resume);
    }

    // Lazy Collection들을 미리 초기화
    private void initializeLazyCollections(Resume resume) {
        // size() 호출로 컬렉션 초기화
        if (resume.getSections() != null) {
            resume.getSections().size();
            // 각 섹션의 연관 엔티티도 초기화
            resume.getSections().forEach(section -> {
                if (section.getResume() != null) {
                    section.getResume().getId(); // resume proxy 초기화
                }
            });
        }

        if (resume.getEducations() != null) {
            resume.getEducations().size();
        }

        if (resume.getCareers() != null) {
            resume.getCareers().size();
        }

        if (resume.getExperiences() != null) {
            resume.getExperiences().size();
        }

        // User 정보도 초기화 (필요한 경우)
        if (resume.getUser() != null) {
            resume.getUser().getId();
        }
    }

    // 기본 정보 생성
    @Transactional
    public ResumeResponse createResume(Long userId, ResumeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Resume resume = Resume.builder()
                .user(user)
                .title(request.getTitle())
                .industry(request.getIndustry())
                .position(request.getPosition())
                .targetCompany(request.getTargetCompany())
                .highlights(request.getHighlights())
                .status(request.getStatus() != null ? request.getStatus() : ResumeStatus.TEMP_SAVED)
                .build();

        Resume saved = resumeRepository.save(resume);
        return new ResumeResponse(saved);
    }

    // 기본 정보 수정
    @Transactional
    public ResumeResponse updateResume(Long resumeId, Long userId, ResumeRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        resume.updateBasicInfo(
                request.getTitle(),
                request.getIndustry(),
                request.getPosition(),
                request.getTargetCompany(),
                request.getHighlights()
        );

        if (request.getStatus() != null) {
            resume.updateStatus(request.getStatus());
        }

        return new ResumeResponse(resume);
    }

    // 삭제
    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));
        resumeRepository.delete(resume);
    }

    // 섹션 저장
    @Transactional
    public void saveSection(Long resumeId, Long userId, SectionRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        // 기존 섹션이 있는지 확인
        Optional<ResumeSection> existingSection = resumeSectionRepository
                .findByResumeIdAndSectionType(resumeId, request.getSectionType());

        if (existingSection.isPresent()) {
            // 기존 섹션 업데이트
            existingSection.get().updateContent(request.getContent());
        } else {
            // 새 섹션 생성
            ResumeSection section = ResumeSection.builder()
                    .resume(resume)
                    .sectionType(request.getSectionType())
                    .content(request.getContent())
                    .wordCount(request.getContent() != null ? request.getContent().length() : 0)
                    .build();
            resumeSectionRepository.save(section);
        }
    }

    // 학력 추가
    @Transactional
    public void addEducation(Long resumeId, Long userId, EducationRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        Education education = Education.builder()
                .resume(resume)
                .schoolName(request.getSchoolName())
                .type(request.getType())
                .level(request.getLevel())
                .major(request.getMajor())
                .additionalMajor(request.getAdditionalMajor())
                .admissionDate(request.getAdmissionDate())
                .graduationDate(request.getGraduationDate())
                .isCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false)
                .build();

        educationRepository.save(education);
    }

    // 경력 추가
    @Transactional
    public void addCareer(Long resumeId, Long userId, CareerRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        Career career = Career.builder()
                .resume(resume)
                .companyName(request.getCompanyName())
                .department(request.getDepartment())
                .positionTitle(request.getPositionTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false)
                .responsibilities(request.getResponsibilities())
                .resignationReason(request.getResignationReason())
                .build();

        careerRepository.save(career);
    }

    // 경험/활동 추가
    @Transactional
    public void addExperience(Long resumeId, Long userId, ExperienceRequest request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        Experience experience = Experience.builder()
                .resume(resume)
                .activityName(request.getActivityName())
                .institution(request.getInstitution())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isCurrent(request.getIsCurrent() != null ? request.getIsCurrent() : false)
                .content(request.getContent())
                .build();

        experienceRepository.save(experience);
    }
}
