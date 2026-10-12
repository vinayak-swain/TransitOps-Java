package com.transitops.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "TransitOps API is running (Java Spring Boot)");
        resp.put("timestamp", Instant.now().toString());

        boolean isOAuth = googleClientId != null && !googleClientId.isBlank() && !googleClientId.contains("dummy");
        resp.put("oauth", isOAuth ? "configured" : "not configured");

        return ResponseEntity.ok(resp);
    }
}
