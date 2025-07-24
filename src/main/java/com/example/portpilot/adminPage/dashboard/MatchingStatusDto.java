package com.example.portpilot.adminPage.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchingStatusDto {
    private String status;
    private long count;
}
