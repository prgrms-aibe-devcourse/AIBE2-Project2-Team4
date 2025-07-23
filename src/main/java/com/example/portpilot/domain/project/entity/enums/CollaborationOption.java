package com.example.portpilot.domain.project.entity.enums;

public enum CollaborationOption {
    NO_PARTNER("파트너와 협업 가능한 인력이 없습니다."),
    HAS_PARTNER("파트너와 협업 가능한 인력이 있습니다.");

    private final String label;
    CollaborationOption(String label) { this.label = label; }
    public String getLabel() { return label; }
}