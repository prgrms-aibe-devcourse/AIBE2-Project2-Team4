package com.example.portpilot.domain.search.repository;

import com.example.portpilot.domain.search.entity.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findByUserIdAndIsBlockedFalseOrderByCreatedAtDesc(Long userId);
    List<SavedSearch> findByUserIdOrderByCreatedAtDesc(Long userId);
}