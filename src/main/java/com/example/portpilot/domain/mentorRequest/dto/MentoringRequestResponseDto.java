package com.example.portpilot.domain.mentorRequest.dto;

import com.example.portpilot.domain.mentorRequest.entity.MentoringRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentoringRequestResponseDto {
    private Long id;
    private String mentorName;
    private String userName;
    private String topic;
    private String message;
    private String status;
    private String createdAt;

    public static MentoringRequestResponseDto fromEntity(MentoringRequest entity) {
        MentoringRequestResponseDto dto = new MentoringRequestResponseDto();
        dto.setId(entity.getId());
        dto.setMentorName(entity.getMentor() != null ? entity.getMentor().getName() : "");  // NPE 방지
        dto.setUserName(entity.getUser().getName());
        dto.setTopic(entity.getTopic());
        dto.setMessage(entity.getMessage() != null ? entity.getMessage() : "No message available");
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt().toString());
        return dto;
    }

}
