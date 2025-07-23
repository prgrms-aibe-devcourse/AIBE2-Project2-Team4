package com.example.portpilot.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ProfileStatsDto {
    private long ongoingCount;
    private long deliveredCount;
    private long issuesCount;
    private Purchases purchases;

    @Getter @AllArgsConstructor
    public static class Purchases {
        private long confirmed;
        private long pendingReviews;
        private long cancelled;
    }
}
