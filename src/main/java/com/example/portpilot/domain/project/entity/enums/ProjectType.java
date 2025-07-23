package com.example.portpilot.domain.project.entity.enums;

public enum ProjectType {
    NEW("신규 프로젝트를 진행하려 합니다."),
    MAINTENANCE("운영 중인 서비스의 리뉴얼 또는 유지보수를 하려 합니다.");

    private final String label;
    ProjectType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
