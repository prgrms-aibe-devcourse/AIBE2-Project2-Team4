package com.example.portpilot.domain.bookmark.service;

import com.example.portpilot.domain.bookmark.entity.Bookmark;
import com.example.portpilot.domain.bookmark.entity.Bookmark.BookmarkType;
import com.example.portpilot.domain.bookmark.repository.BookmarkRepository;
import com.example.portpilot.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    /**
     * 북마크 추가
     */
    @Transactional
    public Bookmark addBookmark(User user, BookmarkType targetType, Long targetId) {
        log.info("북마크 추가 요청 - 사용자: {}, 타입: {}, 대상ID: {}", user.getId(), targetType, targetId);

        // 이미 북마크되어 있는지 확인
        if (bookmarkRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId)) {
            throw new IllegalArgumentException("이미 북마크에 추가된 항목입니다.");
        }

        Bookmark bookmark = new Bookmark(user, targetType, targetId);
        Bookmark savedBookmark = bookmarkRepository.save(bookmark);

        log.info("북마크 추가 완료 - ID: {}", savedBookmark.getId());
        return savedBookmark;
    }

    /**
     * 북마크 제거
     */
    @Transactional
    public void removeBookmark(User user, BookmarkType targetType, Long targetId) {
        log.info("북마크 제거 요청 - 사용자: {}, 타입: {}, 대상ID: {}", user.getId(), targetType, targetId);

        if (!bookmarkRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId)) {
            throw new IllegalArgumentException("북마크에 없는 항목입니다.");
        }

        bookmarkRepository.deleteByUserAndTargetTypeAndTargetId(user, targetType, targetId);
        log.info("북마크 제거 완료");
    }

    /**
     * 북마크 토글 (있으면 제거, 없으면 추가)
     */
    @Transactional
    public boolean toggleBookmark(User user, BookmarkType targetType, Long targetId) {
        log.info("북마크 토글 요청 - 사용자: {}, 타입: {}, 대상ID: {}", user.getId(), targetType, targetId);

        boolean exists = bookmarkRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId);

        if (exists) {
            // 북마크 제거
            bookmarkRepository.deleteByUserAndTargetTypeAndTargetId(user, targetType, targetId);
            log.info("북마크 토글: 제거됨");
            return false; // 제거됨
        } else {
            // 북마크 추가
            Bookmark bookmark = new Bookmark(user, targetType, targetId);
            bookmarkRepository.save(bookmark);
            log.info("북마크 토글: 추가됨");
            return true; // 추가됨
        }
    }

    /**
     * 사용자의 모든 북마크 조회
     */
    public List<Bookmark> getUserBookmarks(User user) {
        log.info("사용자 북마크 전체 조회 - 사용자: {}", user.getId());
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 사용자 ID로 모든 북마크 조회
     */
    public List<Bookmark> getUserBookmarks(Long userId) {
        log.info("사용자 북마크 전체 조회 - 사용자ID: {}", userId);
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 특정 타입 북마크 조회
     */
    public List<Bookmark> getUserBookmarksByType(User user, BookmarkType targetType) {
        log.info("사용자 북마크 타입별 조회 - 사용자: {}, 타입: {}", user.getId(), targetType);
        return bookmarkRepository.findByUserAndTargetTypeOrderByCreatedAtDesc(user, targetType);
    }

    /**
     * 사용자 ID로 특정 타입 북마크 조회
     */
    public List<Bookmark> getUserBookmarksByType(Long userId, BookmarkType targetType) {
        log.info("사용자 북마크 타입별 조회 - 사용자ID: {}, 타입: {}", userId, targetType);
        return bookmarkRepository.findByUserIdAndTargetTypeOrderByCreatedAtDesc(userId, targetType);
    }

    /**
     * 북마크 여부 확인
     */
    public boolean isBookmarked(User user, BookmarkType targetType, Long targetId) {
        return bookmarkRepository.existsByUserAndTargetTypeAndTargetId(user, targetType, targetId);
    }

    /**
     * 사용자 ID로 북마크 여부 확인
     */
    public boolean isBookmarked(Long userId, BookmarkType targetType, Long targetId) {
        return bookmarkRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    /**
     * 특정 대상의 북마크 수 조회
     */
    public Long getBookmarkCount(BookmarkType targetType, Long targetId) {
        return bookmarkRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    /**
     * 북마크 상세 조회
     */
    public Optional<Bookmark> getBookmark(Long bookmarkId) {
        return bookmarkRepository.findById(bookmarkId);
    }

    /**
     * 북마크 삭제 (ID로)
     */
    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        log.info("북마크 삭제 요청 - ID: {}", bookmarkId);

        if (!bookmarkRepository.existsById(bookmarkId)) {
            throw new IllegalArgumentException("존재하지 않는 북마크입니다.");
        }

        bookmarkRepository.deleteById(bookmarkId);
        log.info("북마크 삭제 완료 - ID: {}", bookmarkId);
    }

    /**
     * 사용자의 북마크 통계 조회
     */
    public List<Object[]> getBookmarkStats(User user) {
        return bookmarkRepository.getBookmarkStatsByUser(user);
    }
}