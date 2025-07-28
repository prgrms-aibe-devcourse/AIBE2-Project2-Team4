
package com.example.portpilot.domain.search.repository;

import com.example.portpilot.domain.search.dto.UserSearchDto;
import com.example.portpilot.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSearchRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u " +
            "WHERE u.isDeleted = false " +
            "AND u.isBlocked = false " +
            "AND (:#{#search.keyword} IS NULL OR :#{#search.keyword} = '' OR " +
            "     LOWER(u.name) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%')) OR " +
            "     LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%')) OR " +
            "     LOWER(u.address) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%'))) " +
            "AND (:#{#search.location} IS NULL OR :#{#search.location} = '' OR " +
            "     LOWER(u.address) LIKE LOWER(CONCAT('%', :#{#search.location}, '%')))")
    Page<User> searchUsers(@Param("search") UserSearchDto search, Pageable pageable);

    // 검색 결과 수 계산
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.isDeleted = false " +
            "AND u.isBlocked = false " +
            "AND (:#{#search.keyword} IS NULL OR :#{#search.keyword} = '' OR " +
            "     LOWER(u.name) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%')) OR " +
            "     LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%')) OR " +
            "     LOWER(u.address) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%'))) " +
            "AND (:#{#search.location} IS NULL OR :#{#search.location} = '' OR " +
            "     LOWER(u.address) LIKE LOWER(CONCAT('%', :#{#search.location}, '%')))")
    long countSearchResults(@Param("search") UserSearchDto search);

    @Query("SELECT u FROM User u WHERE " +
            "u.isDeleted = false AND u.isBlocked = false AND " +  // 추가 필요
            "(LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.address) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("location") String location,
                           Pageable pageable);

    // 추후 UserProfile과 연관관계 설정 시 사용할 고급 검색 쿼리
    /*
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN u.userProfile up " +
           "LEFT JOIN up.skills s " +
           "WHERE u.isDeleted = false " +
           "AND u.isBlocked = false " +
           "AND (:#{#search.keyword} IS NULL OR :#{#search.keyword} = '' OR " +
           "     LOWER(u.name) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#search.keyword}, '%'))) " +
           "AND (:#{#search.skills} IS NULL OR :#{#search.skills} IS EMPTY OR s.name IN :#{#search.skills}) " +
           "AND (:#{#search.minExperience} IS NULL OR up.experience >= :#{#search.minExperience}) " +
           "AND (:#{#search.maxExperience} IS NULL OR up.experience <= :#{#search.maxExperience}) " +
           "AND (:#{#search.education} IS NULL OR :#{#search.education} = '' OR up.education = :#{#search.education}) " +
           "AND (:#{#search.location} IS NULL OR :#{#search.location} = '' OR " +
           "     LOWER(u.address) LIKE LOWER(CONCAT('%', :#{#search.location}, '%')))")
    Page<User> searchUsersWithProfile(@Param("search") UserSearchDto search, Pageable pageable);
    */
}