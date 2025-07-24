package com.example.portpilot.domain.mentorRequest.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Getter
@Setter
public class MentoringScheduleDto {
    private LocalDateTime scheduledAt;
    private LocalDateTime proposedAt;
}
