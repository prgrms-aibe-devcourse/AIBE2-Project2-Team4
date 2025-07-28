package com.example.portpilot.domain.search.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private String keyword;              // 키워드 검색
    private List<String> skills;         // 기술 스택
    private Integer minExperience;       // 최소 경력
    private Integer maxExperience;       // 최대 경력
    private String education;            // 학력
    private String location;             // 지역
    private String sortBy;               // 정렬 기준 (created_at, experience, name)
    private String sortDirection;        // 정렬 방향 (asc, desc)
    private int page = 0;                // 페이지 번호
    private int size = 20;               // 페이지 크기

    // 검색 조건이 비어있는지 확인
    public boolean isEmpty() {
        return (keyword == null || keyword.trim().isEmpty()) &&
                (skills == null || skills.isEmpty()) &&
                minExperience == null &&
                maxExperience == null &&
                (education == null || education.trim().isEmpty()) &&
                (location == null || location.trim().isEmpty());
    }
}