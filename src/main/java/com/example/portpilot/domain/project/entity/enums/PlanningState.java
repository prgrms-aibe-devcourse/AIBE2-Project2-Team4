package com.example.portpilot.domain.project.entity.enums;

public enum PlanningState {
    IDEA_ONLY("아이디어만 있는 상태"),
    OUTLINE("필요한 내용을 간단히 정리한 상태"),
    DETAILED("상세한 기획 문서가 존재"),
    PROTOTYPE("프로토타입(샘플 버전)");

    private final String label;
    PlanningState(String label) { this.label = label; }
    public String getLabel() { return label; }
}
