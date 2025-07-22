
package com.example.portpilot.domain.company.repository;

import com.example.portpilot.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // 기업명으로 검색
    Optional<Company> findByName(String name);

    // 기업명에 키워드 포함된 기업들 검색
    List<Company> findByNameContaining(String keyword);

    // 업종으로 검색
    List<Company> findByIndustry(String industry);

    // 지역으로 검색
    List<Company> findByLocationContaining(String location);

    // 기업명과 업종으로 검색
    @Query("SELECT c FROM Company c WHERE c.name LIKE %:name% AND c.industry = :industry")
    List<Company> findByNameContainingAndIndustry(@Param("name") String name, @Param("industry") String industry);
}