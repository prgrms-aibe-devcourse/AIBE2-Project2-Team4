package com.example.portpilot.domain.notification;

import com.example.portpilot.domain.notification.Notification;
import com.example.portpilot.domain.notification.NotificationRepository;
import com.example.portpilot.domain.notification.NotificationType;
import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notiRepo;
    private final UserRepository userRepo;

    /** 프로젝트 요청 승인/거절 시 호출 */
    public void notifyProjectResult(Long userId, Long projectId, boolean approved, String projectTitle) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NotificationType t = approved
                ? NotificationType.REQUEST_APPROVED
                : NotificationType.REQUEST_REJECTED;
        String msg = String.format("%s 프로젝트 참여 요청이 %s되었습니다.",
                projectTitle,
                approved ? "승인" : "거절");
        Notification noti = Notification.builder()
                .user(u)
                .type(t)
                .message(msg)
                .targetUrl("/projects/" + projectId)
                .build();
        notiRepo.save(noti);
    }

    /** 프로필 페이지에서 보여줄 알림 조회 */
    public List<Notification> getNotifications(Long userId) {
        return notiRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
