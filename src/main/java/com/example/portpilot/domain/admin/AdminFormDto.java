package com.example.portpilot.domain.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminFormDto {
    private String name;
    private String email;
    private String password;
    private String address;
}
