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
public class SearchFilterDto {
    // 기본 필터
    private String keyword;
    private String location;

    // 스킬 관련 필터
    private List<String> requiredSkills;    // 필수 스킬
    private List<String> preferredSkills;   // 우대 스킬
    private String skillLevel;              // 스킬 레벨 (BEGINNER, INTERMEDIATE, ADVANCED)

    // 경력 관련 필터
    private Integer minExperience;
    private Integer maxExperience;
    private List<String> experienceTypes;  // 경력 유형 (INTERN, JUNIOR, SENIOR, LEAD)

    // 학력 관련 필터
    private String education;               // 최소 학력
    private List<String> majors;           // 전공 분야
    private String graduationStatus;        // 졸업 상태 (GRADUATED, EXPECTED, ENROLLED)

    // 근무 조건 필터
    private List<String> employmentTypes;  // 고용 형태 (FULL_TIME, PART_TIME, CONTRACT, FREELANCE)
    private String workLocation;           // 근무 지역
    private Boolean remoteWork;            // 원격 근무 가능 여부

    // 연봉 관련 필터
    private Long minSalary;
    private Long maxSalary;
    private String salaryType;             // 연봉 유형 (NEGOTIABLE, FIXED)

    // 기타 필터
    private Integer minAge;
    private Integer maxAge;
    private String gender;                 // 성별 (선택적)
    private Boolean hasPortfolio;         // 포트폴리오 보유 여부
    private Boolean hasCertification;     // 자격증 보유 여부

    // 활동 관련 필터
    private Boolean isActive;             // 최근 활동 여부
    private Integer lastLoginDays;        // 마지막 로그인 일수

    // 추가 검색 옵션
    private Boolean exactMatch;           // 정확히 일치하는 검색
    private List<Long> excludeUserIds;    // 제외할 사용자 ID 목록

    // 필터가 비어있는지 확인
    public boolean isEmpty() {
        return (keyword == null || keyword.trim().isEmpty()) &&
                (location == null || location.trim().isEmpty()) &&
                (requiredSkills == null || requiredSkills.isEmpty()) &&
                (preferredSkills == null || preferredSkills.isEmpty()) &&
                minExperience == null &&
                maxExperience == null &&
                (education == null || education.trim().isEmpty()) &&
                (majors == null || majors.isEmpty()) &&
                minSalary == null &&
                maxSalary == null &&
                minAge == null &&
                maxAge == null;
    }
}