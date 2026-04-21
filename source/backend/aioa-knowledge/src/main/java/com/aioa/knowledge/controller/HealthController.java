package com.aioa.knowledge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String health() {
        return "Knowledge Service is running";
    }
    
    @GetMapping("/vector/status")
    public String vectorStatus() {
        return "Vector service: Ready (Mock mode due to network issues)";
    }
}
