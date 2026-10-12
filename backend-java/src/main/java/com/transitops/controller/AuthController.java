package com.transitops.controller;

import com.transitops.dto.*;
import com.transitops.security.SecurityUtils;
import com.transitops.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Map<String, Object> data = authService.login(req);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("token", data.get("token"));
        response.put("user", data.get("user"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        Map<String, Object> data = authService.register(req);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Account created successfully");
        response.put("token", data.get("token"));
        response.put("user", data.get("user"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }
        Map<String, Object> user = authService.getMe(userId);
        return ResponseEntity.ok(Map.of("success", true, "user", user));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> getUsers() {
        List<Map<String, Object>> users = authService.getUsers();
        return ResponseEntity.ok(Map.of("success", true, "data", users));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable Long id, @RequestBody UpdateRoleRequest req) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Map<String, Object> updated = authService.updateUserRole(id, req.getRole(), currentUserId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User role updated successfully.",
                "data", updated
        ));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateUserStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Map<String, Object> updated = authService.updateUserStatus(id, req.getIsActive(), currentUserId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User status updated successfully.",
                "data", updated
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UpdateProfileRequest req) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Map<String, Object> updated = authService.updateProfile(currentUserId, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully.",
                "user", updated
        ));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest req) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        authService.changePassword(currentUserId, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully."
        ));
    }
}
