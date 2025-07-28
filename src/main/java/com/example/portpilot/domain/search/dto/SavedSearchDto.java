package com.example.portpilot.domain.search.dto;

import com.example.portpilot.domain.search.entity.SavedSearch;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SavedSearchDto {
    private Long id;
    private String name;
    private String searchConditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isBlocked;

    // 검색 조건 요약 정보 (JSON 파싱 없이 표시용)
    private String conditionSummary;
    private int resultCount;  // 마지막 검색 결과 수

    public SavedSearchDto(SavedSearch savedSearch) {
        this.id = savedSearch.getId();
        this.name = savedSearch.getName();
        this.searchConditions = savedSearch.getSearchConditions();
        this.createdAt = savedSearch.getCreatedAt();
        this.updatedAt = savedSearch.getUpdatedAt();
        this.isBlocked = savedSearch.isBlocked();
        this.conditionSummary = generateSummary(savedSearch.getSearchConditions());
    }

    // JSON 조건을 간단한 요약으로 변환
    private String generateSummary(String conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "검색 조건 없음";
        }

        // 간단한 요약 로직 (실제로는 JSON 파싱해서 주요 조건만 표시)
        StringBuilder summary = new StringBuilder();

        if (conditions.contains("keyword")) {
            summary.append("키워드 검색, ");
        }
        if (conditions.contains("skills")) {
            summary.append("스킬 필터, ");
        }
        if (conditions.contains("experience")) {
            summary.append("경력 필터, ");
        }
        if (conditions.contains("location")) {
            summary.append("지역 필터, ");
        }

        String result = summary.toString();
        return result.isEmpty() ? "기본 검색" : result.substring(0, result.length() - 2);
    }
}