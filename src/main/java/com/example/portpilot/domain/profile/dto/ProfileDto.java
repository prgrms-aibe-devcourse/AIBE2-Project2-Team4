package com.example.portpilot.domain.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class ProfileDto {
    private String name;
    private String position;
    private String bio;
    private List<String> skills;
}