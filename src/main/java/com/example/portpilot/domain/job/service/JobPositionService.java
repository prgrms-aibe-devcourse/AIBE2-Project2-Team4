
package com.example.portpilot.domain.job.service;

import com.example.portpilot.domain.job.entity.JobPosition;
import com.example.portpilot.domain.job.repository.JobPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPositionService {

    private final JobPositionRepository jobPositionRepository;

    /**
     * 모든 채용공고 목록 조회 (페이징)
     */
    public Page<JobPosition> getAllJobPositions(Pageable pageable) {
        return jobPositionRepository.findAll(pageable);
    }

    /**
     * 모든 채용공고 목록 조회 (전체)
     */
    public List<JobPosition> getAllJobPositions() {
        return jobPositionRepository.findAll();
    }

    /**
     * 모집중인 채용공고만 조회
     */
    public List<JobPosition> getActiveJobPositions() {
        return jobPositionRepository.findByStatus("모집중");
    }

    /**
     * 제목으로 채용공고 검색
     */
    public List<JobPosition> searchByTitle(String keyword) {
        return jobPositionRepository.findByTitleContaining(keyword);
    }

    /**
     * 지역으로 채용공고 검색
     */
    public List<JobPosition> searchByLocation(String location) {
        return jobPositionRepository.findByLocationContaining(location);
    }

    /**
     * 복합 검색 (제목 + 지역 + 상태)
     */
    public List<JobPosition> searchJobs(String title, String location, String status) {
        if (title == null) title = "";
        if (location == null) location = "";
        if (status == null) status = "모집중";

        return jobPositionRepository.findByTitleAndLocationAndStatus(title, location, status);
    }

    /**
     * 채용공고 상세 조회
     */
    public Optional<JobPosition> getJobPositionById(Long id) {
        return jobPositionRepository.findById(id);
    }

    /**
     * 마감일이 지나지 않은 채용공고 조회
     */
    public List<JobPosition> getJobPositionsBeforeDeadline() {
        return jobPositionRepository.findByDeadlineAfter(LocalDateTime.now());
    }
    // 기존 JobPositionService.java에 추가할 메서드들

    /**
     * 채용공고 등록
     */
    @Transactional
    public JobPosition createJobPosition(JobPosition jobPosition) {
        return jobPositionRepository.save(jobPosition);
    }

    /**
     * 채용공고 수정
     */
    @Transactional
    public JobPosition updateJobPosition(Long id, JobPosition updateJobPosition) {
        JobPosition existingJob = jobPositionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));

        // 수정 가능한 필드들 업데이트
        existingJob.setTitle(updateJobPosition.getTitle());
        existingJob.setDescription(updateJobPosition.getDescription());
        existingJob.setRequirements(updateJobPosition.getRequirements());
        existingJob.setSalary(updateJobPosition.getSalary());
        existingJob.setLocation(updateJobPosition.getLocation());
        existingJob.setExperienceLevel(updateJobPosition.getExperienceLevel());
        existingJob.setDeadline(updateJobPosition.getDeadline());
        existingJob.setStatus(updateJobPosition.getStatus());

        return jobPositionRepository.save(existingJob);
    }

    /**
     * 채용공고 삭제
     */
    @Transactional
    public void deleteJobPosition(Long id) {
        if (!jobPositionRepository.existsById(id)) {
            throw new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id);
        }
        jobPositionRepository.deleteById(id);
    }

    /**
     * 채용공고 상태 변경 (모집중 ↔ 마감)
     */
    @Transactional
    public JobPosition updateJobStatus(Long id, String status) {
        JobPosition jobPosition = jobPositionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채용공고를 찾을 수 없습니다. ID: " + id));

        jobPosition.setStatus(status);
        return jobPositionRepository.save(jobPosition);
    }

}