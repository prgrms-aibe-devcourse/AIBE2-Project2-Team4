package com.example.portpilot.domain.profile.dto;

import lombok.Data;
import java.util.List;

/**
 * PUT /api/profile 요청 바디로 받는 DTO
 */
@Data
public class ProfileUpdateRequest {
    private String position;
    private String bio;
    private List<String> skills;
}