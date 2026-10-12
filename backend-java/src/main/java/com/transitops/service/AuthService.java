package com.transitops.service;

import com.transitops.dto.*;
import com.transitops.entity.User;
import com.transitops.repository.UserRepository;
import com.transitops.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * AuthService — mirrors authController.js logic.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> VALID_ROLES = List.of(
            "fleet_manager", "dispatcher", "safety_officer", "financial_analyst"
    );

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Map<String, Object> login(LoginRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userRepository.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new SecurityException("Invalid email or password."));

        if (!user.getIsActive()) {
            throw new SecurityException("Account is disabled. Contact admin.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new SecurityException("Invalid email or password.");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", userToMap(user));
        return result;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (req.getName() == null || req.getEmail() == null || req.getPassword() == null || req.getRole() == null) {
            throw new IllegalArgumentException("All fields are required.");
        }
        if (!VALID_ROLES.contains(req.getRole())) {
            throw new IllegalArgumentException("Invalid role.");
        }
        if (req.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        String email = req.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already registered.");
        }

        User user = User.builder()
                .name(req.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .isActive(true)
                .provider("local")
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", userToMap(user));
        return result;
    }

    public Map<String, Object> getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("created_at", user.getCreatedAt());
        return result;
    }

    public List<Map<String, Object>> getUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(u -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("email", u.getEmail());
                    map.put("role", u.getRole());
                    map.put("is_active", u.getIsActive());
                    map.put("created_at", u.getCreatedAt());
                    return map;
                })
                .toList();
    }

    @Transactional
    public Map<String, Object> updateUserRole(Long id, String role, Long currentUserId) {
        if (role == null || !VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role.");
        }
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot change your own role.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        user.setRole(role);
        user = userRepository.save(user);
        return userToMap(user);
    }

    @Transactional
    public Map<String, Object> updateUserStatus(Long id, Boolean isActive, Long currentUserId) {
        if (isActive == null) {
            throw new IllegalArgumentException("is_active status is required.");
        }
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        user.setIsActive(isActive);
        user = userRepository.save(user);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("is_active", user.getIsActive());
        return map;
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, UpdateProfileRequest req) {
        if (req.getName() == null || req.getEmail() == null) {
            throw new IllegalArgumentException("Name and email are required.");
        }

        String email = req.getEmail().toLowerCase().trim();
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new IllegalStateException("Email is already taken.");
            }
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        user.setName(req.getName().trim());
        user.setEmail(email);
        user = userRepository.save(user);
        return userToMap(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        if (req.getCurrentPassword() == null || req.getNewPassword() == null) {
            throw new IllegalArgumentException("Current and new passwords are required.");
        }
        if (req.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found."));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new SecurityException("Incorrect current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Google OAuth2 callback — handles all 3 cases from passport.js strategy.
     */
    @Transactional
    public Map<String, Object> handleGoogleOAuth(String googleId, String email, String name,
                                                  String profilePicture, String selectedRole) {
        // CASE A: Returning Google user
        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingByGoogleId.isPresent()) {
            User user = existingByGoogleId.get();
            if (!user.getIsActive()) {
                throw new SecurityException("Account is disabled. Contact admin.");
            }
            if (selectedRole != null && !selectedRole.equals(user.getRole())) {
                throw new SecurityException(
                    "You are already registered as a " + user.getRole().replace("_", " ") +
                    ". Please select the correct role to sign in."
                );
            }
            if (profilePicture != null && !profilePicture.equals(user.getProfilePicture())) {
                user.setProfilePicture(profilePicture);
                userRepository.save(user);
            }

            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("user", userToMapWithPicture(user));
            return result;
        }

        // CASE B: Email exists with provider = 'local'
        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User existing = existingByEmail.get();
            if ("local".equals(existing.getProvider()) || existing.getProvider() == null) {
                throw new SecurityException(
                    "This email is already registered using email and password. Please sign in using your existing credentials."
                );
            }
        }

        // CASE C: New user — auto-register
        List<String> validOAuthRoles = List.of("fleet_manager", "dispatcher", "safety_officer", "financial_analyst", "driver");
        String finalRole = (selectedRole != null && validOAuthRoles.contains(selectedRole)) ? selectedRole : "fleet_manager";

        User newUser = User.builder()
                .name(name)
                .email(email)
                .passwordHash(null)
                .role(finalRole)
                .provider("google")
                .googleId(googleId)
                .profilePicture(profilePicture)
                .isActive(true)
                .build();
        newUser = userRepository.save(newUser);

        String token = jwtTokenProvider.generateToken(newUser.getId(), newUser.getEmail(), newUser.getRole());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", userToMapWithPicture(newUser));
        return result;
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        return map;
    }

    private Map<String, Object> userToMapWithPicture(User user) {
        Map<String, Object> map = userToMap(user);
        map.put("profile_picture", user.getProfilePicture());
        return map;
    }
}
