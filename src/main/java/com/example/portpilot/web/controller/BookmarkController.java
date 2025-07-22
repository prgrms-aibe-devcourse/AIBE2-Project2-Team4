package com.example.portpilot.web.controller;

import com.example.portpilot.domain.bookmark.entity.Bookmark;
import com.example.portpilot.domain.bookmark.entity.Bookmark.BookmarkType;
import com.example.portpilot.domain.bookmark.service.BookmarkService;
import com.example.portpilot.domain.job.entity.JobPosition;
import com.example.portpilot.domain.job.service.JobPositionService;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
@Slf4j
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JobPositionService jobPositionService;

    /**
     * 북마크 목록 페이지
     */
    @GetMapping
    public String bookmarkList(
            @RequestParam(value = "type", required = false, defaultValue = "all") String type,
            Model model) {

        log.info("북마크 목록 조회 - 타입: {}", type);

        // TODO: 현재 로그인한 사용자 정보 가져오기
        // User currentUser = getCurrentUser();
        Long currentUserId = 1L; // 임시로 사용자 ID 1 사용

        List<Bookmark> bookmarks;
        String pageTitle;

        // 타입별 북마크 조회
        switch (type) {
            case "jobs":
                bookmarks = bookmarkService.getUserBookmarksByType(currentUserId, BookmarkType.JOB_POSITION);
                pageTitle = "채용공고 북마크";
                break;
            case "users":
                bookmarks = bookmarkService.getUserBookmarksByType(currentUserId, BookmarkType.USER);
                pageTitle = "구직자 북마크";
                break;
            default:
                bookmarks = bookmarkService.getUserBookmarks(currentUserId);
                pageTitle = "전체 북마크";
                break;
        }

        // 북마크와 연관된 실제 데이터 조회
        List<Map<String, Object>> bookmarkData = new ArrayList<>();

        for (Bookmark bookmark : bookmarks) {
            Map<String, Object> data = new HashMap<>();
            data.put("bookmark", bookmark);

            if (bookmark.getTargetType() == BookmarkType.JOB_POSITION) {
                // 채용공고 정보 조회
                jobPositionService.getJobPositionById(bookmark.getTargetId())
                        .ifPresent(job -> data.put("jobPosition", job));
            } else if (bookmark.getTargetType() == BookmarkType.USER) {
                // 구직자 정보 조회 (나중에 UserService 구현시)
                // userService.getUserById(bookmark.getTargetId())
                //     .ifPresent(user -> data.put("targetUser", user));
                data.put("targetUser", null); // 임시
            }

            bookmarkData.add(data);
        }

        // 북마크 통계
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) bookmarks.size());
        stats.put("jobs", (long) bookmarkService.getUserBookmarksByType(currentUserId, BookmarkType.JOB_POSITION).size());
        stats.put("users", (long) bookmarkService.getUserBookmarksByType(currentUserId, BookmarkType.USER).size());

        model.addAttribute("bookmarkData", bookmarkData);
        model.addAttribute("currentType", type);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("stats", stats);

        return "bookmarks/list";
    }

    /**
     * 북마크 토글 (AJAX)
     */
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @RequestParam BookmarkType targetType,
            @RequestParam Long targetId) {

        log.info("북마크 토글 요청 - 타입: {}, 대상ID: {}", targetType, targetId);

        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: 현재 로그인한 사용자 정보 가져오기
            // User currentUser = getCurrentUser();
            Long currentUserId = 1L; // 임시
            User tempUser = new User(); // 임시 User 객체
            tempUser.setId(currentUserId);

            boolean isAdded = bookmarkService.toggleBookmark(tempUser, targetType, targetId);
            Long bookmarkCount = bookmarkService.getBookmarkCount(targetType, targetId);

            response.put("success", true);
            response.put("bookmarked", isAdded);
            response.put("message", isAdded ? "북마크에 추가되었습니다." : "북마크에서 제거되었습니다.");
            response.put("bookmarkCount", bookmarkCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("북마크 토글 실패", e);
            response.put("success", false);
            response.put("message", "북마크 처리에 실패했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 북마크 삭제
     */
    @PostMapping("/{bookmarkId}/delete")
    public String deleteBookmark(@PathVariable Long bookmarkId) {
        log.info("북마크 삭제 요청 - ID: {}", bookmarkId);

        try {
            bookmarkService.deleteBookmark(bookmarkId);
            return "redirect:/bookmarks?message=북마크가 삭제되었습니다.";
        } catch (Exception e) {
            log.error("북마크 삭제 실패", e);
            return "redirect:/bookmarks?error=북마크 삭제에 실패했습니다.";
        }
    }

    /**
     * 북마크 상태 확인 (AJAX)
     */
    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkBookmark(
            @RequestParam BookmarkType targetType,
            @RequestParam Long targetId) {

        // TODO: 현재 로그인한 사용자 정보 가져오기
        Long currentUserId = 1L; // 임시

        Map<String, Object> response = new HashMap<>();
        boolean isBookmarked = bookmarkService.isBookmarked(currentUserId, targetType, targetId);
        Long bookmarkCount = bookmarkService.getBookmarkCount(targetType, targetId);

        response.put("bookmarked", isBookmarked);
        response.put("bookmarkCount", bookmarkCount);

        return ResponseEntity.ok(response);
    }
}