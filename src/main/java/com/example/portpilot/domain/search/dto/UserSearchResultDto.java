package com.example.portpilot.domain.search.dto;

import com.example.portpilot.domain.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserSearchResultDto {
    private Long id;
    private String name;
    private String email;
    private String address;
    private List<String> skills;
    private Integer experience;
    private String education;
    private LocalDateTime createdAt;
    private boolean isBookmarked;

    public UserSearchResultDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.createdAt = user.getCreatedAt();
        // UserProfile이 있다면 추가 정보 설정
        // TODO: UserProfile 연관관계 확인 후 skills, experience, education 설정
    }
}