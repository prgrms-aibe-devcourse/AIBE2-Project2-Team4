package com.example.portpilot.adminPage.userManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserListResponseDto {
    private List<UserDetailDto> users;
    private int totalPages;
}
