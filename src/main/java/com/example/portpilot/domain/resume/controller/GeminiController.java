package com.example.portpilot.domain.resume.controller;

import com.example.portpilot.domain.resume.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/chat")
    public ResponseEntity<?> gemini() {
        try {
            return ResponseEntity.ok().body(geminiService.getContents("안녕! 너는 누구야?"));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}