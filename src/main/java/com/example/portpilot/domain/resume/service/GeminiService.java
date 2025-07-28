package com.example.portpilot.domain.resume.service;

import com.example.portpilot.domain.resume.dto.ChatRequest;
import com.example.portpilot.domain.resume.dto.ChatResponse;
import com.example.portpilot.domain.resume.dto.ResumeResponse;
import com.example.portpilot.domain.resume.dto.SectionRequest;
import com.example.portpilot.domain.resume.entity.Career;
import com.example.portpilot.domain.resume.entity.Education;
import com.example.portpilot.domain.resume.entity.SectionType;
import com.example.portpilot.domain.resume.repository.ResumeSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Qualifier("geminiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${GEMINI_URL}")
    private String apiUrl;

    @Value("${GEMINI_KEY}")
    private String geminiApiKey;

    private final ResumeService resumeService;
    private final ResumeSectionRepository resumeSectionRepository;

    public String getContents(String prompt) {

        // Gemini에 요청 전송
        String requestUrl = apiUrl + "?key=" + geminiApiKey;

        ChatRequest request = new ChatRequest(prompt);
        ChatResponse response = restTemplate.postForObject(requestUrl, request, ChatResponse.class);

        String message = response.getCandidates().get(0).getContent().getParts().get(0).getText().toString();

        return message;
    }

    @Transactional
    public void generateCoverLetterSections(Long userId, Long resumeId) {
        try {
            log.info("자소서 생성 시작 - userId: {}, resumeId: {}", userId, resumeId);

            // 이력서 정보 조회 (매개변수 순서 수정)
            ResumeResponse resume = resumeService.getResume(resumeId, userId);

            // 각 섹션별로 생성 및 저장
            generateAndSaveSection(resume, userId, SectionType.GROWTH);
            generateAndSaveSection(resume, userId, SectionType.MOTIVATION);
            generateAndSaveSection(resume, userId, SectionType.PERSONALITY);
            generateAndSaveSection(resume, userId, SectionType.GOALS);

            log.info("자소서 생성 완료 - resumeId: {}", resumeId);
        } catch (Exception e) {
            log.error("자소서 생성 중 오류 발생 - userId: {}, resumeId: {}", userId, resumeId, e);
            throw new RuntimeException("자소서 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private void generateAndSaveSection(ResumeResponse resume, Long userId, SectionType sectionType) {
        try {
            log.debug("섹션 생성 시작 - sectionType: {}", sectionType);

            // 프롬프트 생성
            String prompt = createPromptForSection(resume, sectionType);

            // Gemini API 호출
            String content = getContents(prompt);

            // 섹션 저장
            SectionRequest sectionRequest = new SectionRequest(sectionType, content);
            resumeService.saveSection(resume.getId(), userId, sectionRequest);

            log.debug("섹션 생성 완료 - sectionType: {}", sectionType);
        } catch (Exception e) {
            log.error("섹션 생성 중 오류 발생 - sectionType: {}", sectionType, e);
            throw new RuntimeException("섹션 생성 중 오류가 발생했습니다: " + sectionType, e);
        }
    }

    private String createPromptForSection(ResumeResponse resume, SectionType sectionType) {
        StringBuilder baseInfo = new StringBuilder();

        baseInfo.append("다음은 지원자의 정보입니다:\n");
        baseInfo.append("- 희망 직군: ").append(resume.getPosition()).append("\n");
        baseInfo.append("- 희망 업계: ").append(resume.getIndustry()).append("\n");
        if (resume.getTargetCompany() != null && !resume.getTargetCompany().isEmpty()) {
            baseInfo.append("- 목표 회사: ").append(resume.getTargetCompany()).append("\n");
        }

        if (resume.getHighlights() != null && !resume.getHighlights().isEmpty()) {
            baseInfo.append("- 특이사항: ").append(resume.getHighlights()).append("\n");
        }

        // 학력 정보 추가
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            baseInfo.append("- 학력: ");
            for (Education education : resume.getEducations()) {
                baseInfo.append(education.getSchoolName())
                        .append(" ")
                        .append(education.getMajor() != null ? education.getMajor() : "")
                        .append(", ");
            }
            baseInfo.append("\n");
        }

        // 경력 정보 추가
        if (resume.getCareers() != null && !resume.getCareers().isEmpty()) {
            baseInfo.append("- 경력: ");
            for (Career career : resume.getCareers()) {
                baseInfo.append(career.getCompanyName())
                        .append(" ")
                        .append(career.getPositionTitle() != null ? career.getPositionTitle() : "")
                        .append(", ");
            }
            baseInfo.append("\n");
        }

        String specificPrompt = "";
        switch (sectionType) {
            case GROWTH:
                specificPrompt = "위 정보를 바탕으로 '성장과정'에 대한 자기소개서를 작성해 주세요. " +
                        "지원자의 성장 배경과 가치관 형성 과정을 중심으로 400-500자 내외로 작성해 주세요.";
                break;
            case MOTIVATION:
                specificPrompt = "위 정보를 바탕으로 '지원동기'에 대한 자기소개서를 작성해 주세요. " +
                        "해당 회사와 직무에 지원하는 구체적인 이유를 400-500자 내외로 작성해 주세요.";
                break;
            case PERSONALITY:
                specificPrompt = "위 정보를 바탕으로 '성격의 장단점'에 대한 자기소개서를 작성해 주세요. " +
                        "업무에 도움이 되는 장점과 개선하고자 하는 단점을 400-500자 내외로 작성해 주세요.";
                break;
            case GOALS:
                specificPrompt = "위 정보를 바탕으로 '입사 후 포부'에 대한 자기소개서를 작성해 주세요. " +
                        "입사 후 목표와 회사에 기여할 수 있는 방안을 400-500자 내외로 작성해 주세요.";
                break;
        }

        return baseInfo.toString() + "\n" + specificPrompt;
    }
}
