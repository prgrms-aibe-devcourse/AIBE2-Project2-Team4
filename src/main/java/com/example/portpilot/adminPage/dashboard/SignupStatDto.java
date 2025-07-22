package com.example.portpilot.adminPage.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class SignupStatDto {
    private Date date;
    private Long count;

    public SignupStatDto(Date date, Long count) {
        this.date = date;
        this.count = count;
    }
}
