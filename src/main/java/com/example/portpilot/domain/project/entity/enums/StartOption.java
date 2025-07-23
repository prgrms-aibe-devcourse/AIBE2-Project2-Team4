package com.example.portpilot.domain.project.entity.enums;

public enum StartOption {
    IMMEDIATE("계약 체결 이후, 즉시 시작"),
    UNDECIDED("미정");

    private final String label;
    StartOption(String label) { this.label = label; }
    public String getLabel() { return label; }
}
