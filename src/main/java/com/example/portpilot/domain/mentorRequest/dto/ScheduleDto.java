package com.example.portpilot.domain.mentorRequest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleDto {
    private LocalDateTime proposedAt;
}
