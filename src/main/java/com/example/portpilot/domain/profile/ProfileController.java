// src/main/java/com/example/portpilot/domain/profile/ProfileController.java
package com.example.portpilot.domain.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/profile") @RequiredArgsConstructor
public class ProfileController {
    private final ProfileService service;

    @GetMapping
    public ResponseEntity<ProfileDto> profile() {
        return ResponseEntity.ok(service.getProfile());
    }

    @GetMapping("/stats")
    public ResponseEntity<ProfileStatsDto> stats() {
        return ResponseEntity.ok(service.getStats());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityDto>> activity(
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(service.getRecentActivity(size));
    }
}

