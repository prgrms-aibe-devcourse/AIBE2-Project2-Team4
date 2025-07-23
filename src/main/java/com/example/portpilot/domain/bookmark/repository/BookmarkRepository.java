package com.example.portpilot.domain.bookmark.repository;

import com.example.portpilot.domain.bookmark.entity.Bookmark;
import com.example.portpilot.domain.bookmark.entity.Bookmark.BookmarkType;
import com.example.portpilot.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 특정 사용자의 모든 북마크 조회 (최신순)
    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);

    // 사용자 ID로 북마크 조회
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 타입의 북마크만 조회
    List<Bookmark> findByUserAndTargetTypeOrderByCreatedAtDesc(User user, BookmarkType targetType);

    // 사용자 ID와 타입으로 북마크 조회
    List<Bookmark> findByUserIdAndTargetTypeOrderByCreatedAtDesc(Long userId, BookmarkType targetType);

    // 특정 대상이 북마크되어 있는지 확인
    Optional<Bookmark> findByUserAndTargetTypeAndTargetId(User user, BookmarkType targetType, Long targetId);

    // 사용자 ID로 북마크 여부 확인
    Optional<Bookmark> findByUserIdAndTargetTypeAndTargetId(Long userId, BookmarkType targetType, Long targetId);

    // 북마크 존재 여부 확인
    boolean existsByUserAndTargetTypeAndTargetId(User user, BookmarkType targetType, Long targetId);

    // 북마크 존재 여부 확인 (사용자 ID로)
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, BookmarkType targetType, Long targetId);

    // 특정 대상의 북마크 수 조회
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.targetType = :targetType AND b.targetId = :targetId")
    Long countByTargetTypeAndTargetId(@Param("targetType") BookmarkType targetType, @Param("targetId") Long targetId);

    // 북마크 삭제 (특정 대상)
    void deleteByUserAndTargetTypeAndTargetId(User user, BookmarkType targetType, Long targetId);

    // 북마크 삭제 (사용자 ID로)
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, BookmarkType targetType, Long targetId);

    // 사용자의 북마크 통계
    @Query("SELECT b.targetType, COUNT(b) FROM Bookmark b WHERE b.user = :user GROUP BY b.targetType")
    List<Object[]> getBookmarkStatsByUser(@Param("user") User user);
}