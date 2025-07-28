package com.example.portpilot.domain.notification;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Override
    public void notifyProjectResult(Long userId, Long projectId, boolean approved, String projectTitle) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String message = approved
                ? String.format("프로젝트 '%s' 참여 요청이 승인되었습니다.", projectTitle)
                : String.format("프로젝트 '%s' 참여 요청이 거절되었습니다.", projectTitle);

        String targetUrl = "/projects/" + projectId;

        Notification notif = Notification.builder()
                .user(user)
                .message(message)
                .targetUrl(targetUrl)
                .createdAt(LocalDateTime.now())
                .type(NotificationType.PROJECT_RESULT)
                .build();

        notificationRepository.save(notif);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}