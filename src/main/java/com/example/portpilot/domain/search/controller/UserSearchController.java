package com.example.portpilot.domain.search.controller;

import com.example.portpilot.domain.search.dto.UserSearchDto;
import com.example.portpilot.domain.search.dto.UserSearchResultDto;
import com.example.portpilot.domain.search.dto.SavedSearchDto;
import com.example.portpilot.domain.search.dto.SearchFilterDto;
import com.example.portpilot.domain.search.entity.SavedSearch;
import com.example.portpilot.domain.search.service.UserSearchService;
import com.example.portpilot.domain.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/search/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping
    public String searchPage(UserSearchDto searchDto,
                             Model model,
                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        model.addAttribute("searchDto", searchDto);

        if (!searchDto.isEmpty()) {
            Long currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
            Page<UserSearchResultDto> searchResults = userSearchService.searchUsers(searchDto, currentUserId);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("currentPage", searchDto.getPage());
            model.addAttribute("hasResults", true);
            model.addAttribute("totalResults", searchResults.getTotalElements());
        } else {
            model.addAttribute("hasResults", false);
        }

        return "search/user-search";
    }

    // 고급 검색 페이지
    @GetMapping("/advanced")
    public String advancedSearchPage(SearchFilterDto filterDto,
                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                     @RequestParam(defaultValue = "desc") String sortDirection,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     Model model,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        model.addAttribute("filterDto", filterDto);

        if (!filterDto.isEmpty()) {
            Long currentUserId = userPrincipal != null ? userPrincipal.getId() : null;
            Page<UserSearchResultDto> searchResults = userSearchService.searchUsersWithFilter(
                    filterDto, sortBy, sortDirection, page, size, currentUserId);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("currentPage", page);
            model.addAttribute("hasResults", true);
            model.addAttribute("totalResults", searchResults.getTotalElements());
        } else {
            model.addAttribute("hasResults", false);
        }

        return "search/advanced-search";
    }

    @PostMapping("/save")
    public String saveSearchCondition(@RequestParam String name,
                                      UserSearchDto searchDto,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal,
                                      RedirectAttributes redirectAttributes) {
        if (userPrincipal == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/users/login";
        }

        try {
            userSearchService.saveSearchCondition(userPrincipal.getId(), name, searchDto);
            redirectAttributes.addFlashAttribute("success", "검색 조건이 저장되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "검색 조건 저장에 실패했습니다: " + e.getMessage());
        }

        return "redirect:/search/users/saved";
    }

    @GetMapping("/saved")
    public String savedSearches(Model model,
                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return "redirect:/users/login";
        }

        List<SavedSearchDto> savedSearches = userSearchService.getUserSavedSearches(userPrincipal.getId());
        model.addAttribute("savedSearches", savedSearches);

        return "search/saved-searches";
    }

    @GetMapping("/saved/{id}")
    public String loadSavedSearch(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            UserSearchDto searchDto = userSearchService.loadSearchCondition(id);
            // 검색 조건을 파라미터로 전달
            redirectAttributes.addAttribute("keyword", searchDto.getKeyword());
            redirectAttributes.addAttribute("location", searchDto.getLocation());
            redirectAttributes.addAttribute("minExperience", searchDto.getMinExperience());
            redirectAttributes.addAttribute("maxExperience", searchDto.getMaxExperience());
            redirectAttributes.addAttribute("education", searchDto.getEducation());
            redirectAttributes.addAttribute("sortBy", searchDto.getSortBy());
            redirectAttributes.addAttribute("sortDirection", searchDto.getSortDirection());

            return "redirect:/search/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/search/users/saved";
        }
    }

    @PostMapping("/saved/{id}/delete")
    public String deleteSavedSearch(@PathVariable Long id,
                                    @AuthenticationPrincipal UserPrincipal userPrincipal,
                                    RedirectAttributes redirectAttributes) {
        if (userPrincipal == null) {
            return "redirect:/users/login";
        }

        try {
            userSearchService.deleteSavedSearch(id, userPrincipal.getId());
            redirectAttributes.addFlashAttribute("success", "저장된 검색 조건이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/search/users/saved";
    }
}