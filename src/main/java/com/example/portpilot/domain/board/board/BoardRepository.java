package com.example.portpilot.domain.board.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b " +
            "WHERE b.isBlocked = false " +
            "AND (:jobType IS NULL OR TRIM(b.jobType) = :jobType) " +
            "AND (:techStack IS NULL OR TRIM(b.techStack) = :techStack) " +
            "AND (:keyword IS NULL OR b.title LIKE CONCAT('%', :keyword, '%') OR b.content LIKE CONCAT('%', :keyword, '%'))")
    Page<Board> findByFilter(@Param("jobType") String jobType,
                             @Param("techStack") String techStack,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    @Query("SELECT b FROM Board b WHERE b.user.id = :userId AND b.isBlocked = false")
    Page<Board> findByUserId(Long userId, Pageable pageable);

    @Query(
            value = "SELECT b FROM Board b WHERE b.title LIKE %:keyword%",
            countQuery = "SELECT COUNT(b) FROM Board b WHERE b.title LIKE %:keyword%"
    )
    Page<Board> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


}
