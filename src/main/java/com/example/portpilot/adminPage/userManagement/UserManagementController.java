package com.example.portpilot.adminPage.userManagement;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserFormDto;
import com.example.portpilot.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/user-management")
public class UserManagementController {

    private final UserService userService;

    @GetMapping
    public String showUserPage() {
        return "admin/userManagement";
    }

    @GetMapping("/list")
    @ResponseBody
    public UserListResponseDto listUsers(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "") String keyword) {
        PageRequest pageable = PageRequest.of(page - 1, 10);
        Page<UserDetailDto> result = userService.searchUsers(keyword, pageable);
        return new UserListResponseDto(result.getContent(), result.getTotalPages());
    }


    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        model.addAttribute("user", user);
        return "admin/userManagementDetail";
    }


    @PostMapping("/update")
    public String updateUser(UserDetailDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setDeleted(dto.isDeleted());
        user.setBlockedUntil(dto.getBlockedUntil());

        userService.updateUser(user);
        return "redirect:/admin/user-management";
    }

}
