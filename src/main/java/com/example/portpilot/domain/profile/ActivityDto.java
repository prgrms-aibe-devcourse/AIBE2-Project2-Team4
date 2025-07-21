// src/main/java/com/example/portpilot/domain/profile/ActivityDto.java
package com.example.portpilot.domain.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class ActivityDto {
    private LocalDateTime date;
    private String project;
    private String role;
    private String status;
}

