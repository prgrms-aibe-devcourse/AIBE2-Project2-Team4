package com.example.portpilot.domain.portfolioAI.service;

import com.example.portpilot.domain.portfolioAI.dto.*;
import com.example.portpilot.domain.resume.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioGuideService {

    private final FileAnalysisService fileAnalysisService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public PortfolioGuideResponse generateOutline(PortfolioGuideRequest request) {
        log.info("포트폴리오 개요 생성 시작");

        try {
            // 1. 파일 유효성 검사
            validateFile(request.getFile());

            // 2. 직접 파일 읽기 시도
            String fileContent = extractFileContent(request.getFile());
            log.info("파일 내용 추출 결과: {}", fileContent != null ? "성공" : "실패");

            // 3. 프롬프트 생성
            String prompt = createPrompt(request.getJobSubcategory(), fileContent);

            // 4. AI 호출
            String aiResponse = geminiService.getContents(prompt);

            // 5. 응답 파싱
            List<PageGuide> pages = parseResponse(aiResponse);

            return PortfolioGuideResponse.builder()
                    .jobCategory(request.getJobCategory())
                    .jobSubcategory(request.getJobSubcategory())
                    .fileName(request.getFile().getOriginalFilename())
                    .pages(pages)
                    .build();

        } catch (Exception e) {
            log.error("포트폴리오 생성 오류", e);
            return createErrorResponse(request);
        }
    }

    private String extractFileContent(MultipartFile file) {
        try {
            log.info("파일 정보: 이름={}, 크기={}, 타입={}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            // 직접 바이트 읽기
            byte[] bytes = file.getBytes();
            log.info("바이트 읽기 성공: {} bytes", bytes.length);

            if (bytes.length == 0) {
                return null;
            }

            // UTF-8로 변환
            String content = new String(bytes, StandardCharsets.UTF_8);
            log.info("원본 텍스트 길이: {} chars", content.length());

            // 파일이 너무 크면 요약
            if (content.length() > 10000) {
                log.info("파일이 너무 큽니다. 요약 처리합니다.");
                content = summarizeContent(content);
                log.info("요약 후 텍스트 길이: {} chars", content.length());
            }

            return content;

        } catch (Exception e) {
            log.error("파일 읽기 실패", e);
            return null;
        }
    }

    private String summarizeContent(String content) {
        StringBuilder summary = new StringBuilder();

        // 파일 기본 정보
        summary.append("=== 파일 요약 ===\n");
        summary.append("총 길이: ").append(content.length()).append("자\n\n");

        String[] lines = content.split("\n");
        summary.append("총 줄 수: ").append(lines.length).append("줄\n\n");

        // 처음 30줄 추출
        summary.append("=== 파일 앞부분 (처음 30줄) ===\n");
        for (int i = 0; i < Math.min(30, lines.length); i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                summary.append(line).append("\n");
            }
        }

        // 중간 부분에서 20줄 추출
        if (lines.length > 60) {
            summary.append("\n=== 파일 중간부분 ===\n");
            int middle = lines.length / 2;
            for (int i = middle - 10; i < middle + 10 && i < lines.length; i++) {
                if (i >= 0) {
                    String line = lines[i].trim();
                    if (!line.isEmpty()) {
                        summary.append(line).append("\n");
                    }
                }
            }
        }

        // 마지막 20줄 추출
        if (lines.length > 50) {
            summary.append("\n=== 파일 뒷부분 (마지막 20줄) ===\n");
            for (int i = Math.max(0, lines.length - 20); i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    summary.append(line).append("\n");
                }
            }
        }

        // 키워드 분석
        summary.append("\n=== 주요 키워드 분석 ===\n");
        String lowerContent = content.toLowerCase();

        String[] keywords = {"프로젝트", "project", "개발", "디자인", "마케팅", "기획", "경험", "기술",
                "java", "python", "javascript", "react", "spring", "mysql", "aws"};

        List<String> foundKeywords = new ArrayList<>();
        for (String keyword : keywords) {
            if (lowerContent.contains(keyword)) {
                foundKeywords.add(keyword);
            }
        }

        if (!foundKeywords.isEmpty()) {
            summary.append("발견된 키워드: ").append(String.join(", ", foundKeywords)).append("\n");
        }

        return summary.toString();
    }

    private String createPrompt(String jobSubcategory, String fileContent) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 포트폴리오 작성 전문가입니다.\n\n");
        prompt.append("희망 직무: ").append(jobSubcategory).append("\n\n");

        if (fileContent != null && !fileContent.trim().isEmpty()) {
            prompt.append("업로드된 파일 분석 결과:\n");
            prompt.append(fileContent).append("\n\n");
        } else {
            prompt.append("파일 내용이 제공되지 않았습니다.\n\n");
        }

        prompt.append("위 분석 결과를 바탕으로 이 지원자에게 맞는 ").append(jobSubcategory).append(" 포트폴리오 구성을 제안해주세요.\n");
        prompt.append("지원자의 현재 경험과 강점을 활용하면서 부족한 부분은 보완 방법을 제시하세요.\n\n");

        prompt.append("반드시 다음 형식으로만 응답하세요. 다른 설명은 하지 마세요:\n\n");

        prompt.append("페이지1:\n");
        prompt.append("제목: 기본정보\n");
        prompt.append("내용: 이 지원자가 기본정보 페이지에 포함해야 할 구체적인 내용을 한 문단으로 작성\n");
        prompt.append("팁1: 실무에서 바로 활용할 수 있는 첫번째 작성 팁\n");
        prompt.append("팁2: 효과적인 어필을 위한 두번째 팁\n");
        prompt.append("팁3: 채용담당자가 중요하게 보는 세번째 포인트\n\n");

        prompt.append("페이지2:\n");
        prompt.append("제목: 기술및역량\n");
        prompt.append("내용: 이 지원자가 기술 페이지에 포함해야 할 구체적인 내용\n");
        prompt.append("팁1: 실무에서 바로 활용할 수 있는 첫번째 작성 팁\n");
        prompt.append("팁2: 효과적인 어필을 위한 두번째 팁\n");
        prompt.append("팁3: 채용담당자가 중요하게 보는 세번째 포인트\n\n");

        prompt.append("페이지3:\n");
        prompt.append("제목: 프로젝트경험\n");
        prompt.append("내용: 이 지원자가 프로젝트 페이지에 포함해야 할 구체적인 내용\n");
        prompt.append("팁1: 실무에서 바로 활용할 수 있는 첫번째 작성 팁\n");
        prompt.append("팁2: 효과적인 어필을 위한 두번째 팁\n");
        prompt.append("팁3: 채용담당자가 중요하게 보는 세번째 포인트\n\n");

        prompt.append("페이지4:\n");
        prompt.append("제목: 경력및활동\n");
        prompt.append("내용: 이 지원자가 경력 페이지에 포함해야 할 구체적인 내용\n");
        prompt.append("팁1: 실무에서 바로 활용할 수 있는 첫번째 작성 팁\n");
        prompt.append("팁2: 효과적인 어필을 위한 두번째 팁\n");
        prompt.append("팁3: 채용담당자가 중요하게 보는 세번째 포인트\n\n");

        prompt.append("페이지5:\n");
        prompt.append("제목: 자격증및교육\n");
        prompt.append("내용: 이 지원자가 자격증 페이지에 포함해야 할 구체적인 내용\n");
        prompt.append("팁1: 실무에서 바로 활용할 수 있는 첫번째 작성 팁\n");
        prompt.append("팁2: 효과적인 어필을 위한 두번째 팁\n");
        prompt.append("팁3: 채용담당자가 중요하게 보는 세번째 포인트\n\n");

        prompt.append("반드시 위의 5개 페이지를 모두 작성해주세요.\n");
        prompt.append("각 페이지마다 제목, 내용, 팁1, 팁2, 팁3를 반드시 포함하세요.\n");

        return prompt.toString();
    }

    private List<PageGuide> parseResponse(String aiResponse) {
        List<PageGuide> pages = new ArrayList<>();

        try {
            log.info("=== AI 응답 파싱 시작 ===");
            log.info("AI 응답 길이: {}", aiResponse.length());
            log.info("AI 응답 내용:\n{}", aiResponse);

            // "페이지" 키워드로 분할
            String[] sections = aiResponse.split("(?=페이지\\d+:)");
            log.info("분할된 섹션 수: {}", sections.length);

            for (int i = 0; i < sections.length; i++) {
                String section = sections[i].trim();
                log.info("섹션 {}: {}", i, section.substring(0, Math.min(100, section.length())));

                if (section.isEmpty() || !section.startsWith("페이지")) {
                    continue;
                }

                PageGuide page = parseSection(section, i);
                if (page != null) {
                    pages.add(page);
                    log.info("페이지 {} 파싱 성공: {}", page.getPageNumber(), page.getTitle());
                } else {
                    log.warn("페이지 {} 파싱 실패", i);
                }
            }

        } catch (Exception e) {
            log.error("응답 파싱 실패", e);
        }

        log.info("최종 파싱된 페이지 수: {}", pages.size());

        if (pages.isEmpty()) {
            log.warn("파싱 실패로 기본 페이지 사용");
            pages = getDefaultPages();
        }

        return pages;
    }

    private PageGuide parseSection(String section, int pageNum) {
        try {
            log.info("=== 섹션 {} 파싱 시작 ===", pageNum);
            log.info("섹션 내용: {}", section);

            String[] lines = section.split("\n");
            log.info("라인 수: {}", lines.length);

            String title = "";
            String content = "";
            List<String> tips = new ArrayList<>();

            for (String line : lines) {
                line = line.trim();
                log.info("처리 중인 라인: '{}'", line);

                if (line.startsWith("제목:")) {
                    title = line.substring(3).trim();
                    log.info("제목 파싱: '{}'", title);
                } else if (line.startsWith("내용:")) {
                    // 내용이 여러 줄일 수 있으므로 이후 모든 텍스트를 수집
                    StringBuilder contentBuilder = new StringBuilder();
                    contentBuilder.append(line.substring(3).trim());

                    // 다음 라인들도 내용에 포함 (팁이 나올 때까지)
                    for (int i = Arrays.asList(lines).indexOf(line) + 1; i < lines.length; i++) {
                        String nextLine = lines[i].trim();
                        if (nextLine.startsWith("팁1:") || nextLine.startsWith("팁2:") || nextLine.startsWith("팁3:") || nextLine.startsWith("제목:")) {
                            break;
                        }
                        if (!nextLine.isEmpty()) {
                            contentBuilder.append(" ").append(nextLine);
                        }
                    }

                    content = contentBuilder.toString().trim();
                    log.info("내용 파싱 (전체): '{}'", content);
                } else if (line.startsWith("팁1:")) {
                    String tip = line.substring(3).trim();
                    tips.add(tip);
                    log.info("팁1 파싱: '{}'", tip);
                } else if (line.startsWith("팁2:")) {
                    String tip = line.substring(3).trim();
                    tips.add(tip);
                    log.info("팁2 파싱: '{}'", tip);
                } else if (line.startsWith("팁3:")) {
                    String tip = line.substring(3).trim();
                    tips.add(tip);
                    log.info("팁3 파싱: '{}'", tip);
                } else if (!line.isEmpty() && content.isEmpty() && !title.isEmpty()) {
                    // 제목 다음에 오는 일반 텍스트를 내용으로 처리
                    content = line;
                    log.info("일반 텍스트를 내용으로 파싱: '{}'", content);
                }
            }

            log.info("파싱 결과 - 제목: '{}', 내용: '{}', 팁 수: {}", title, content, tips.size());

            if (title.isEmpty()) {
                title = "페이지 " + pageNum;
                log.info("빈 제목으로 기본값 사용: '{}'", title);
            }

            if (content.isEmpty()) {
                content = "AI가 생성한 포트폴리오 구성 내용입니다.";
                log.warn("빈 내용으로 기본값 사용");
            }

            return PageGuide.builder()
                    .pageNumber(String.valueOf(pageNum))
                    .title(title)
                    .description("포트폴리오 구성 가이드")
                    .items(Arrays.asList(
                            ContentItem.builder()
                                    .title("포함할 내용")
                                    .content(content)
                                    .tips(tips)
                                    .build()
                    ))
                    .build();

        } catch (Exception e) {
            log.error("섹션 파싱 실패", e);
            return null;
        }
    }

    private List<PageGuide> getDefaultPages() {
        List<PageGuide> pages = new ArrayList<>();

        pages.add(PageGuide.builder()
                .pageNumber("1")
                .title("기본 정보")
                .description("포트폴리오 첫 페이지")
                .items(Arrays.asList(
                        ContentItem.builder()
                                .title("포함할 내용")
                                .content("이름, 연락처, 이메일, 간단한 자기소개를 작성하세요.")
                                .tips(Arrays.asList("전문적인 이메일 사용", "자기소개는 간결하게", "직무 관련 키워드 포함"))
                                .build()
                ))
                .build());

        pages.add(PageGuide.builder()
                .pageNumber("2")
                .title("기술 역량")
                .description("보유 기술 소개")
                .items(Arrays.asList(
                        ContentItem.builder()
                                .title("포함할 내용")
                                .content("프로그래밍 언어, 프레임워크, 도구 등을 나열하세요.")
                                .tips(Arrays.asList("관련도 높은 기술 우선", "숙련도 표시", "경험과 연결"))
                                .build()
                ))
                .build());

        pages.add(PageGuide.builder()
                .pageNumber("3")
                .title("프로젝트 경험")
                .description("주요 프로젝트")
                .items(Arrays.asList(
                        ContentItem.builder()
                                .title("포함할 내용")
                                .content("대표 프로젝트 2-3개를 선별하여 상세히 설명하세요.")
                                .tips(Arrays.asList("결과물 링크 포함", "역할과 기여도 명시", "성과 강조"))
                                .build()
                ))
                .build());

        pages.add(PageGuide.builder()
                .pageNumber("4")
                .title("경력 및 활동")
                .description("관련 경험")
                .items(Arrays.asList(
                        ContentItem.builder()
                                .title("포함할 내용")
                                .content("인턴십, 동아리, 봉사활동 등 관련 경험을 정리하세요.")
                                .tips(Arrays.asList("배운 점 강조", "팀워크 경험 포함", "직무 연관성 설명"))
                                .build()
                ))
                .build());

        pages.add(PageGuide.builder()
                .pageNumber("5")
                .title("자격증 및 교육")
                .description("학습 이력")
                .items(Arrays.asList(
                        ContentItem.builder()
                                .title("포함할 내용")
                                .content("관련 자격증, 온라인 강의, 교육 이수 내역을 정리하세요.")
                                .tips(Arrays.asList("최신 자격증 우선", "실무 연관성 강조", "지속 학습 의지 어필"))
                                .build()
                ))
                .build());

        return pages;
    }

    private PortfolioGuideResponse createErrorResponse(PortfolioGuideRequest request) {
        return PortfolioGuideResponse.builder()
                .jobCategory(request.getJobCategory())
                .jobSubcategory(request.getJobSubcategory())
                .fileName(request.getFile().getOriginalFilename())
                .pages(getDefaultPages())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("파일이 없습니다.");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("파일이 너무 큽니다.");
        }
    }
}