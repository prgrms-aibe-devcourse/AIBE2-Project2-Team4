package com.example.portpilot.domain.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminMainController {

    @GetMapping("/admin")
    public String adminMain() {
        return "admin/adminMain";
    }
}
