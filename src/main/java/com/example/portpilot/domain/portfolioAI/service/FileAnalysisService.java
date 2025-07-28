package com.example.portpilot.domain.portfolioAI.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class FileAnalysisService {

    public String extractTextFromFile(MultipartFile file) {
        log.info("=== FileAnalysisService.extractTextFromFile 시작 ===");
        log.info("파일 정보 - 이름: {}, 크기: {}, 타입: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file == null) {
            log.error("파일이 null입니다");
            throw new RuntimeException("파일이 null입니다");
        }

        if (file.isEmpty()) {
            log.error("파일이 비어있습니다");
            throw new RuntimeException("파일이 비어있습니다");
        }

        try {
            log.info("file.getBytes() 호출 시도");
            byte[] fileBytes = file.getBytes();
            log.info("file.getBytes() 성공. 바이트 크기: {}", fileBytes.length);

            if (fileBytes.length == 0) {
                log.error("파일 바이트가 0입니다");
                throw new RuntimeException("파일 바이트가 0입니다");
            }

            String contentType = file.getContentType();
            log.info("Content-Type: {}", contentType);

            if (contentType == null) {
                log.warn("Content-Type이 null - 직접 문자열 읽기 시도");
                return tryDirectStringRead(fileBytes);
            }

            switch (contentType) {
                case "application/pdf":
                    log.info("PDF 파일 처리 시작");
                    return extractTextFromPdf(fileBytes);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    log.info("DOCX 파일 처리 시작");
                    return extractTextFromDocx(fileBytes);
                case "text/plain":
                    log.info("TXT 파일 처리 시작");
                    return extractTextFromTxt(fileBytes);
                default:
                    log.warn("지원하지 않는 파일 형식: {} - 직접 읽기 시도", contentType);
                    return tryDirectStringRead(fileBytes);
            }
        } catch (Exception e) {
            log.error("=== extractTextFromFile에서 예외 발생 ===", e);
            log.error("예외 클래스: {}", e.getClass().getSimpleName());
            log.error("예외 메시지: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("원인: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("파일 내용을 읽을 수 없습니다: " + e.getMessage(), e);
        }
    }

    private String extractTextFromPdf(byte[] fileBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            log.info("PDF 텍스트 추출 성공. 추출된 텍스트 길이: {}", text.length());
            return text;
        } catch (IOException e) {
            log.error("PDF 파일 처리 실패", e);
            throw new RuntimeException("PDF 파일을 처리할 수 없습니다: " + e.getMessage(), e);
        }
    }

    private String extractTextFromDocx(byte[] fileBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String paragraphText = paragraph.getText();
                if (paragraphText != null && !paragraphText.trim().isEmpty()) {
                    text.append(paragraphText).append("\n");
                }
            }

            String result = text.toString();
            log.info("DOCX 텍스트 추출 성공. 추출된 텍스트 길이: {}", result.length());
            return result;
        } catch (IOException e) {
            log.error("DOCX 파일 처리 실패", e);
            throw new RuntimeException("DOCX 파일을 처리할 수 없습니다: " + e.getMessage(), e);
        }
    }

    private String extractTextFromTxt(byte[] fileBytes) {
        try {
            String text = new String(fileBytes, StandardCharsets.UTF_8);
            log.info("TXT 텍스트 추출 성공. 추출된 텍스트 길이: {}", text.length());
            return text;
        } catch (Exception e) {
            log.error("TXT 파일 처리 실패", e);
            throw new RuntimeException("TXT 파일을 처리할 수 없습니다: " + e.getMessage(), e);
        }
    }

    private String tryDirectStringRead(byte[] fileBytes) {
        log.info("=== tryDirectStringRead 시작 ===");
        log.info("바이트 배열 크기: {}", fileBytes.length);

        try {
            String text = new String(fileBytes, StandardCharsets.UTF_8);
            log.info("UTF-8 읽기 성공. 텍스트 길이: {}", text.length());

            if (text.length() > 0) {
                log.info("텍스트 앞부분 100자: '{}'", text.substring(0, Math.min(100, text.length())));
            }

            return text;
        } catch (Exception e) {
            log.error("UTF-8 읽기 실패 - ISO-8859-1 시도", e);
            try {
                String text = new String(fileBytes, "ISO-8859-1");
                log.info("ISO-8859-1 읽기 성공. 텍스트 길이: {}", text.length());
                return text;
            } catch (Exception e2) {
                log.error("모든 인코딩 시도 실패", e2);
                return "파일을 텍스트로 변환할 수 없습니다.";
            }
        }
    }
}