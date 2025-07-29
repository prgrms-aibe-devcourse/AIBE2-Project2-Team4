package com.example.portpilot.domain.resume.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@NoArgsConstructor
public class ExportRequest {
    private String format;
}
