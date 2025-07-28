// src/main/java/com/example/portpilot/domain/notification/Notification.java
package com.example.portpilot.domain.notification;

import com.example.portpilot.domain.user.User;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림을 받을 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 알림 메시지 */
    @Column(nullable = false)
    private String message;

    /** 클릭 시 이동할 URL */
    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    /** 생성 시각 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 알림 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
}