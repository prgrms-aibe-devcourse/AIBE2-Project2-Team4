package com.example.portpilot.domain.search.service;

import com.example.portpilot.domain.search.dto.UserSearchDto;
import com.example.portpilot.domain.search.dto.UserSearchResultDto;
import com.example.portpilot.domain.search.dto.SavedSearchDto;
import com.example.portpilot.domain.search.dto.SearchFilterDto;
import com.example.portpilot.domain.search.entity.SavedSearch;
import com.example.portpilot.domain.search.repository.UserSearchRepository;
import com.example.portpilot.domain.search.repository.SavedSearchRepository;
import com.example.portpilot.domain.bookmark.service.BookmarkService;
import com.example.portpilot.domain.bookmark.entity.Bookmark.BookmarkType; // 추가 필요
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {

    private final UserSearchRepository userSearchRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final BookmarkService bookmarkService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public Page<UserSearchResultDto> searchUsers(UserSearchDto searchDto, Long currentUserId) {
        Sort sort = createSort(searchDto.getSortBy(), searchDto.getSortDirection());
        PageRequest pageRequest = PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);

        Page<User> userPage = userSearchRepository.searchUsers(searchDto, pageRequest);

        return userPage.map(user -> {
            UserSearchResultDto dto = new UserSearchResultDto(user);
            // 북마크 상태 설정 - BookmarkType.USER 사용
            if (currentUserId != null) {
                dto.setBookmarked(bookmarkService.isBookmarked(currentUserId, BookmarkType.USER, user.getId()));
            }
            return dto;
        });
    }

    // 고급 필터를 사용한 검색
    public Page<UserSearchResultDto> searchUsersWithFilter(SearchFilterDto filterDto,
                                                           String sortBy,
                                                           String sortDirection,
                                                           int page,
                                                           int size,
                                                           Long currentUserId) {
        Sort sort = createSort(sortBy, sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // TODO: 고급 필터 검색 로직 구현 (UserProfile 연관관계 설정 후)
        // 현재는 기본 검색으로 처리
        UserSearchDto basicSearch = convertFilterToBasicSearch(filterDto);
        return searchUsers(basicSearch, currentUserId);
    }

    @Transactional
    public SavedSearch saveSearchCondition(Long userId, String name, UserSearchDto searchDto) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            String conditionsJson = objectMapper.writeValueAsString(searchDto);
            SavedSearch savedSearch = new SavedSearch(user, name, conditionsJson);
            return savedSearchRepository.save(savedSearch);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("검색 조건 저장 중 오류가 발생했습니다.", e);
        }
    }

    public UserSearchDto loadSearchCondition(Long savedSearchId) {
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 검색 조건을 찾을 수 없습니다."));

        if (savedSearch.isBlocked()) {
            throw new IllegalStateException("차단된 검색 조건입니다.");
        }

        try {
            return objectMapper.readValue(savedSearch.getSearchConditions(), UserSearchDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("검색 조건 로드 중 오류가 발생했습니다.", e);
        }
    }

    public List<SavedSearchDto> getUserSavedSearches(Long userId) {
        List<SavedSearch> savedSearches = savedSearchRepository.findByUserIdAndIsBlockedFalseOrderByCreatedAtDesc(userId);
        return savedSearches.stream()
                .map(SavedSearchDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSavedSearch(Long savedSearchId, Long userId) {
        SavedSearch savedSearch = savedSearchRepository.findById(savedSearchId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 검색 조건을 찾을 수 없습니다."));

        if (!savedSearch.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        savedSearchRepository.delete(savedSearch);
    }

    // 검색 결과 수 계산 (저장된 검색 조건의 결과 수 표시용)
    public long countSearchResults(UserSearchDto searchDto) {
        return userSearchRepository.countSearchResults(searchDto);
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        String property;
        switch (sortBy != null ? sortBy.toLowerCase() : "created_at") {
            case "name":
                property = "name";
                break;
            case "experience":
                property = "experience"; // UserProfile과 연관시 수정 필요
                break;
            case "updated_at":
                property = "updatedAt";
                break;
            default:
                property = "createdAt";
                break;
        }

        return Sort.by(direction, property);
    }

    // 고급 필터를 기본 검색으로 변환 (임시)
    private UserSearchDto convertFilterToBasicSearch(SearchFilterDto filterDto) {
        UserSearchDto searchDto = new UserSearchDto();
        searchDto.setKeyword(filterDto.getKeyword());
        searchDto.setLocation(filterDto.getLocation());
        searchDto.setMinExperience(filterDto.getMinExperience());
        searchDto.setMaxExperience(filterDto.getMaxExperience());
        searchDto.setEducation(filterDto.getEducation());
        return searchDto;
    }
}