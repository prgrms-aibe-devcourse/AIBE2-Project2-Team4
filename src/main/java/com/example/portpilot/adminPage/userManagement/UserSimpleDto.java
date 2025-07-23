package com.example.portpilot.adminPage.userManagement;

import com.example.portpilot.domain.user.User;
import lombok.Getter;

@Getter
public class UserSimpleDto {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String createdAt;
    private boolean isDeleted;
    private boolean isBlocked;
    private String deletedAt;
    private String blockedAt;
    private String blockedUntil;
    private String status;

    public UserSimpleDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate().toString() : "-";
        this.isDeleted = user.isDeleted();
        this.isBlocked = user.isBlocked();
        this.deletedAt = user.getDeletedAt() != null ? user.getDeletedAt().toString() : null;
        this.blockedAt = user.getBlockedAt() != null ? user.getBlockedAt().toString() : null;
        this.blockedUntil = user.getBlockedUntil() != null ? user.getBlockedUntil().toString() : null;
        this.status = user.isDeleted() ? "탈퇴" : (user.isBlocked() ? "차단" : "정상");
    }
}
