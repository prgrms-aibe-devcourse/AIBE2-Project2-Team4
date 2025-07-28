package com.example.portpilot.domain.resume.service;

import com.example.portpilot.domain.resume.dto.*;
import com.example.portpilot.domain.resume.entity.*;
import com.example.portpilot.domain.resume.repository.*;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumeList(Long userId) {
        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);

        // 각 resume의 lazy collection 초기화
        resumes.forEach(this::initializeLazyCollections);

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

        if (request.getTitle() != null || request.getIndustry() != null ||
                request.getPosition() != null || request.getTargetCompany() != null ||
                request.getHighlights() != null) {

            resume.updateBasicInfo(
                    request.getTitle() != null ? request.getTitle() : resume.getTitle(),
                    request.getIndustry() != null ? request.getIndustry() : resume.getIndustry(),
                    request.getPosition() != null ? request.getPosition() : resume.getPosition(),
                    request.getTargetCompany() != null ? request.getTargetCompany() : resume.getTargetCompany(),
                    request.getHighlights() != null ? request.getHighlights() : resume.getHighlights()
            );
        }

        // 상태만 변경하는 요청인 경우 처리
        if (request.getStatus() != null) {
            resume.updateStatus(request.getStatus());
        }

        initializeLazyCollections(resume);

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

    // 파일 내보내기
    @Transactional(readOnly = true)
    public Resource exportResume(Long resumeId, Long userId, String format) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));

        initializeLazyCollections(resume);

        switch (format.toLowerCase()) {
            case "txt":
                return exportAsText(resume);
            case "pdf":
                return exportAsPdf(resume);
            case "docx":
                return exportAsDocx(resume);
            default:
                throw new RuntimeException("지원하지 않는 파일 형식입니다: " + format);
        }
    }

    // 텍스트 파일로 내보내기
    private Resource exportAsText(Resume resume) {
        StringBuilder content = new StringBuilder();

        // 제목
        content.append(resume.getTitle()).append("\n");
        content.append("=".repeat(50)).append("\n\n");

        // 기본 정보
        if (resume.getIndustry() != null || resume.getPosition() != null) {
            content.append("■ 지원 정보\n");
            if (resume.getIndustry() != null) {
                content.append("• 희망 업계: ").append(resume.getIndustry()).append("\n");
            }
            if (resume.getPosition() != null) {
                content.append("• 희망 직무: ").append(resume.getPosition()).append("\n");
            }
            if (resume.getTargetCompany() != null) {
                content.append("• 목표 회사: ").append(resume.getTargetCompany()).append("\n");
            }
            content.append("\n");
        }

        // 자소서 섹션들
        if (resume.getSections() != null && !resume.getSections().isEmpty()) {
            content.append("■ 자기소개서\n\n");

            // 섹션 타입별 제목 매핑
            Map<String, String> sectionTitles = Map.of(
                    "GROWTH", "성장과정",
                    "MOTIVATION", "지원동기",
                    "PERSONALITY", "성격의 장단점",
                    "GOALS", "입사 후 포부",
                    "ASPIRATION", "입사 후 포부"
            );

            int sectionNumber = 1;
            for (ResumeSection section : resume.getSections()) {
                String sectionTitle = sectionTitles.getOrDefault(
                        section.getSectionType().name(),
                        section.getSectionType().name()
                );

                content.append(sectionNumber++).append(". ").append(sectionTitle).append("\n");
                content.append("-".repeat(30)).append("\n");
                content.append(section.getContent() != null ? section.getContent() : "").append("\n\n");
            }
        }

        // 학력 정보
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            content.append("■ 학력\n");
            for (Education education : resume.getEducations()) {
                content.append("• ").append(education.getSchoolName());
                if (education.getMajor() != null) {
                    content.append(" - ").append(education.getMajor());
                }
                content.append("\n");
            }
            content.append("\n");
        }

        // 경력 정보
        if (resume.getCareers() != null && !resume.getCareers().isEmpty()) {
            content.append("■ 경력\n");
            for (Career career : resume.getCareers()) {
                content.append("• ").append(career.getCompanyName());
                if (career.getPositionTitle() != null) {
                    content.append(" - ").append(career.getPositionTitle());
                }
                if (career.getResponsibilities() != null) {
                    content.append("\n  ").append(career.getResponsibilities());
                }
                content.append("\n");
            }
            content.append("\n");
        }

        // 활동/경험
        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            content.append("■ 활동/경험\n");
            for (Experience experience : resume.getExperiences()) {
                content.append("• ").append(experience.getActivityName());
                if (experience.getInstitution() != null) {
                    content.append(" - ").append(experience.getInstitution());
                }
                if (experience.getContent() != null) {
                    content.append("\n  ").append(experience.getContent());
                }
                content.append("\n");
            }
            content.append("\n");
        }

        byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(bytes);
    }

    // PDF 내보내기
    private Resource exportAsPdf(Resume resume) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // 한글 폰트 로드
            PdfFont regularFont;
            PdfFont boldFont;

            try {
                // 클래스패스에서 폰트 파일 로드
                regularFont = PdfFontFactory.createFont("fonts/NotoSansKR-Regular.ttf", "Identity-H");
                boldFont = PdfFontFactory.createFont("fonts/NotoSansKR-Bold.ttf", "Identity-H");
            } catch (Exception e) {
                // 폰트 파일이 없으면 온라인에서 로드 시도
                try {
                    // 대체 폰트 (시스템 폰트 시도)
                    regularFont = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                    boldFont = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                } catch (Exception e2) {
                    throw new RuntimeException("한글 폰트를 로드할 수 없습니다. fonts/ 폴더에 NotoSansKR-Regular.ttf 파일을 추가해주세요.");
                }
            }

            // === 페이지 여백 설정 ===
            document.setMargins(50, 50, 50, 50);

            // === 제목 ===
            document.add(new Paragraph(resume.getTitle())
                    .setFont(boldFont)
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30)
                    .setBold());

            // === 구분선 ===
            document.add(new Paragraph("━".repeat(30))
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30));

            // === 자소서 섹션들 ===
            if (resume.getSections() != null && !resume.getSections().isEmpty()) {

                // 섹션 제목 매핑
                Map<String, String> sectionTitles = Map.of(
                        "GROWTH", "성장과정",
                        "MOTIVATION", "지원동기",
                        "PERSONALITY", "성격의 장단점",
                        "GOALS", "입사 후 포부",
                        "ASPIRATION", "입사 후 포부"
                );

                int sectionNumber = 1;
                for (ResumeSection section : resume.getSections()) {
                    String sectionTitle = sectionTitles.getOrDefault(
                            section.getSectionType().name(),
                            section.getSectionType().name()
                    );

                    // 섹션 제목
                    document.add(new Paragraph(sectionNumber + ". " + sectionTitle)
                            .setFont(boldFont)
                            .setFontSize(16)
                            .setMarginTop(25)
                            .setMarginBottom(15)
                            .setBold());

                    // 섹션 내용
                    String content = section.getContent() != null ? section.getContent() : "";

                    // 긴 텍스트를 문단별로 나누기
                    String[] paragraphs = content.split("\n\n");
                    for (String paragraph : paragraphs) {
                        if (!paragraph.trim().isEmpty()) {
                            document.add(new Paragraph(paragraph.trim())
                                    .setFont(regularFont)
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.JUSTIFIED)
                                    .setMarginBottom(12)
                                    .setFirstLineIndent(20)); // 들여쓰기
                        }
                    }

                    sectionNumber++;

                    // 섹션 간 여백
                    document.add(new Paragraph(" ").setMarginBottom(10));
                }
            } else {
                // 자소서 섹션이 없는 경우
                document.add(new Paragraph("자소서 내용이 없습니다.")
                        .setFont(regularFont)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(50));
            }

            // === 페이지 하단 정보 ===
            document.add(new Paragraph(" ").setMarginTop(30));
            document.add(new Paragraph("━".repeat(30))
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            // 생성 날짜
            java.time.LocalDate today = java.time.LocalDate.now();
            document.add(new Paragraph("생성일: " + today.toString())
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10));

            // 문서 닫기
            document.close();

            return new ByteArrayResource(baos.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // DOCX 내보내기
    private Resource exportAsDocx(Resume resume) {
        try {
            XWPFDocument document = new XWPFDocument();

            // 제목
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(resume.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("맑은 고딕");

            // 빈 줄
            document.createParagraph();
            document.createParagraph();

            if (resume.getSections() != null && !resume.getSections().isEmpty()) {
                Map<String, String> sectionTitles = Map.of(
                        "GROWTH", "성장과정",
                        "MOTIVATION", "지원동기",
                        "PERSONALITY", "성격의 장단점",
                        "GOALS", "입사 후 포부",
                        "ASPIRATION", "입사 후 포부"
                );

                int sectionNumber = 1;
                for (ResumeSection section : resume.getSections()) {
                    String sectionTitle = sectionTitles.getOrDefault(
                            section.getSectionType().name(),
                            section.getSectionType().name()
                    );

                    // 섹션 제목
                    XWPFParagraph sectionTitlePara = document.createParagraph();
                    XWPFRun sectionTitleRun = sectionTitlePara.createRun();
                    sectionTitleRun.setText(sectionNumber++ + ". " + sectionTitle);
                    sectionTitleRun.setBold(true);
                    sectionTitleRun.setFontSize(14);
                    sectionTitleRun.setFontFamily("맑은 고딕");

                    // 섹션 내용
                    XWPFParagraph contentPara = document.createParagraph();
                    XWPFRun contentRun = contentPara.createRun();
                    String content = section.getContent() != null ? section.getContent() : "";
                    contentRun.setText(content);
                    contentRun.setFontSize(11);
                    contentRun.setFontFamily("맑은 고딕");

                    // 빈 줄
                    document.createParagraph();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            document.close();

            return new ByteArrayResource(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("DOCX 생성 중 오류가 발생했습니다.", e);
        }
    }
}
