package com.example.portpilot.domain.portfolio.service;

import com.example.portpilot.domain.portfolio.dto.PortfolioRequest;
import com.example.portpilot.domain.portfolio.dto.PortfolioResponse;
import com.example.portpilot.domain.portfolio.entity.Portfolio;
import com.example.portpilot.domain.portfolio.entity.PortfolioStatus;
import com.example.portpilot.domain.portfolio.repository.PortfolioRepository;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserService userService;

    // application.properties 의 uploadPath 값을 주입받아 사용
    @Value("${uploadPath}")
    private String uploadPath;

    /** 1) 기본 포트폴리오 생성 (파일 무시) */
    @Transactional
    public PortfolioResponse createPortfolio(Long userId, PortfolioRequest req) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 사용자 ID: " + userId));

        Portfolio p = Portfolio.builder()
                .title(req.getTitle())
                .background(req.getBackground())
                .results(req.getResults())
                .features(req.getFeatures())
                .stages(req.getStages())
                .details(req.getDetails())
                .description(req.getDescription())
                .link(req.getLink())
                .tags(req.getTags())
                .category(req.getCategory())
                .images(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(PortfolioStatus.PENDING_REVIEW)
                .user(user)
                .build();

        Portfolio saved = portfolioRepository.save(p);
        return toResponse(saved);
    }

    /** 2) 파일 업로드 포함 생성 */
    @Transactional
    public Long createPortfolioWithFiles(Long userId, PortfolioRequest req, MultipartFile[] files) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 사용자 ID: " + userId));

        List<String> storedNames = storeFiles(files);

        Portfolio p = Portfolio.builder()
                .title(req.getTitle())
                .background(req.getBackground())
                .results(req.getResults())
                .features(req.getFeatures())
                .stages(req.getStages())
                .details(req.getDetails())
                .description(req.getDescription())
                .link(req.getLink())
                .tags(req.getTags())
                .category(req.getCategory())
                .images(storedNames.isEmpty() ? null : String.join(",", storedNames))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(PortfolioStatus.PENDING_REVIEW)
                .user(user)
                .build();

        return portfolioRepository.save(p).getId();
    }

    /** 3) 사용자별 포트폴리오 목록 조회 */
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getUserPortfolios(Long userId) {
        userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 사용자 ID: " + userId));

        return portfolioRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 4) 단건 조회 (상세 페이지용) */
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long id) {
        Portfolio p = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 없습니다. id=" + id));
        return toResponse(p);
    }

    /** 5) 전체 탐색 (검색 + 페이징) */
    @Transactional(readOnly = true)
    public Page<PortfolioResponse> searchPortfolios(String keyword, Pageable pageable) {
        Page<Portfolio> page = (keyword == null || keyword.isBlank())
                ? portfolioRepository.findAll(pageable)
                : portfolioRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        return page.map(this::toResponse);
    }

    /** 6) 파일 업로드 포함 수정 */
    @Transactional
    public void updatePortfolioWithFiles(Long id, PortfolioRequest req, MultipartFile[] files) {
        Portfolio p = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 없습니다. id=" + id));

        p.setTitle(req.getTitle());
        p.setBackground(req.getBackground());
        p.setResults(req.getResults());
        p.setFeatures(req.getFeatures());
        p.setStages(req.getStages());
        p.setDetails(req.getDetails());
        p.setDescription(req.getDescription());
        p.setLink(req.getLink());
        p.setTags(req.getTags());
        p.setCategory(req.getCategory());
        p.setUpdatedAt(LocalDateTime.now());

        List<String> existing = p.getImages() != null
                ? new ArrayList<>(Arrays.asList(p.getImages().split(",")))
                : new ArrayList<>();
        List<String> added = storeFiles(files);
        if (!added.isEmpty()) {
            existing.addAll(added);
            p.setImages(String.join(",", existing));
        }
        portfolioRepository.save(p);
    }

    /**
     * MultipartFile[] → uploadPath 폴더에 저장 → 저장된 파일명 리스트 반환
     */
    private List<String> storeFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        Path uploadRoot = Paths.get(uploadPath);
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉터리 생성 실패: " + uploadRoot, e);
        }

        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String original = file.getOriginalFilename();
            String ext = "";
            int dot = (original != null ? original.lastIndexOf('.') : -1);
            if (dot > 0) ext = original.substring(dot);

            String stored = UUID.randomUUID().toString() + ext;
            try {
                file.transferTo(uploadRoot.resolve(stored).toFile());
                result.add(stored);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + original, e);
            }
        }
        return result;
    }

    /** 엔티티 → DTO 변환 */
    private PortfolioResponse toResponse(Portfolio p) {
        List<String> images = (p.getImages() != null)
                ? Arrays.asList(p.getImages().split(","))
                : Collections.emptyList();

        return PortfolioResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .background(p.getBackground())
                .results(p.getResults())
                .features(p.getFeatures())
                .stages(p.getStages())
                .details(p.getDetails())
                .description(p.getDescription())
                .link(p.getLink())
                .tags(p.getTags())
                .category(p.getCategory())
                .images(images)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .authorName(p.getUser().getName())
                .status(p.getStatus())
                .build();
    }
}