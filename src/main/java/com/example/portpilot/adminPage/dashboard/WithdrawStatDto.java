package com.example.portpilot.adminPage.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class WithdrawStatDto {
    private Date date;
    private Long count;

    public WithdrawStatDto(Date date, Long count) {
        this.date = date;
        this.count = count;
    }
}
