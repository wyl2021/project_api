package com.example.project.controller;

import com.example.project.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    // 检查API可用性
    @GetMapping("/health")
    public ResponseEntity<?> checkApiAvailability() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("apiVersion", "1.0.0");
        
        return new ResponseEntity<>(new ApiResponse(true, "API可用", healthInfo), HttpStatus.OK);
    }
}