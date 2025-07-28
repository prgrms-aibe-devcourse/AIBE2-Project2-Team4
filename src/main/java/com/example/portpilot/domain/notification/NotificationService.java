package com.example.portpilot.domain.notification;

import java.util.List;

public interface NotificationService {
    /** 승인/거절 결과를 사용자에게 알림으로 저장 */
    void notifyProjectResult(Long userId, Long projectId, boolean approved, String projectTitle);

    /** 사용자의 모든 알림 조회 */
    List<Notification> getNotifications(Long userId);
}