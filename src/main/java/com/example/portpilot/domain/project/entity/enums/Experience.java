package com.example.portpilot.domain.project.entity.enums;

public enum Experience {
    NONE("IT 프로젝트 관리 경험 없음"),
    SOME("IT 프로젝트 관리 경험 있음");

    private final String label;
    Experience(String label) { this.label = label; }
    public String getLabel() { return label; }
}