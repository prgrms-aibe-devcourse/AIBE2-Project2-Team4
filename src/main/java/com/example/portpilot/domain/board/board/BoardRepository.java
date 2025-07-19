package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b " +
            "WHERE (:jobType IS NULL OR b.jobType = :jobType) " +
            "AND (:techStack IS NULL OR b.techStack = :techStack) " +
            "AND (:keyword IS NULL OR " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Board> findByFilter(@Param("jobType") String jobType,
                             @Param("techStack") String techStack,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    Page<Board> findByUser(User user, Pageable pageable);

}
