package com.example.portpilot.domain.project.repository;

import com.example.portpilot.domain.project.entity.Participation;  // 엔티티 import
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    /** 특정 사용자가 참여한 모든 Participation 조회 */
    List<Participation> findByUserId(Long userId);

    /** 특정 사용자가 특정 프로젝트에 이미 참여했는지 여부 */
    boolean existsByUserIdAndProjectId(Long userId, Long projectId);
}
