package com.example.portpilot.domain.project.repository;

import com.example.portpilot.domain.project.entity.Participation;
import com.example.portpilot.domain.project.entity.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    /** 같은 프로젝트에 같은 유저가 이미 해당 상태인지 검사 */
    boolean existsByProjectIdAndUserIdAndStatus(Long projectId, Long userId, ParticipationStatus status);

    /** 특정 프로젝트·유저의 Participation 조회 */
    Optional<Participation> findByProjectIdAndUserId(Long projectId, Long userId);

    /** 프로젝트별, 상태별 요청/참여 조회 */
    List<Participation> findByProjectIdAndStatus(Long projectId, ParticipationStatus status);

    /** 소유자별, 상태별 전체 요청 리스트 (join fetch owner) */
    List<Participation> findByProjectOwnerIdAndStatus(Long ownerId, ParticipationStatus status);

    List<Participation> findByUserIdAndStatus(Long userId, ParticipationStatus status);


}