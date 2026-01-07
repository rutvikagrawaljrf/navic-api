package com.example.navic.controllers;

import com.example.navic.dto.LoginResponse;
import com.example.navic.dto.RegisterRequest;
import com.example.navic.models.User;
import com.example.navic.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {
    @Autowired
    private UserService userService;

    private static final long TOKEN_EXPIRY_SECONDS = 7L * 24 * 60 * 60;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String password) {
        String token = userService.register(username, password);
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        return ResponseEntity.ok(new LoginResponse(token, TOKEN_EXPIRY_SECONDS));
    }

    @PostMapping("/register/full")
    public ResponseEntity<?> registerFull(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (request.password() == null || request.password().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (request.phone() == null || request.phone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number is required"));
        }
        if (request.fullName() == null || request.fullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full name is required"));
        }

        Map<String, Object> result = userService.registerFull(request);

        if (!(boolean) result.get("success")) {
            return ResponseEntity.badRequest().body(Map.of("error", result.get("error")));
        }

        return ResponseEntity.ok(Map.of(
                "token", result.get("token"),
                "expiresInSeconds", TOKEN_EXPIRY_SECONDS,
                "userId", result.get("userId")
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = userService.login(username, password);

        if (!(boolean) result.get("success")) {
            return ResponseEntity.status(401).body(Map.of("error", result.get("error")));
        }

        return ResponseEntity.ok(Map.of(
                "token", result.get("token"),
                "expiresInSeconds", TOKEN_EXPIRY_SECONDS,
                "userId", result.get("userId"),
                "fullName", result.get("fullName") != null ? result.get("fullName") : "",
                "role", result.get("role") != null ? result.get("role") : "USER"
        ));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());
        profile.put("emergencyContact", user.getEmergencyContact());
        profile.put("emergencyContactPhone", user.getEmergencyContactPhone());
        profile.put("latitude", user.getLatitude());
        profile.put("longitude", user.getLongitude());
        profile.put("manualAddress", user.getManualAddress());
        profile.put("fetchedAddress", user.getFetchedAddress());
        profile.put("city", user.getCity());
        profile.put("state", user.getState());
        profile.put("pincode", user.getPincode());
        profile.put("isAvailableForRescue", user.isAvailableForRescue());
        profile.put("rescueRadiusKm", user.getRescueRadiusKm());
        profile.put("role", user.getRole());
        profile.put("sosCount", user.getSosCount());
        profile.put("rescueCount", user.getRescueCount());
        profile.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String userId,
            @RequestBody Map<String, Object> updates) {

        User user = userService.updateProfile(userId, updates);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PutMapping("/location")
    public ResponseEntity<?> updateLocation(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        User user = userService.updateLocation(userId, latitude, longitude);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Location updated successfully"));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyUsers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double radiusKm,
            @RequestParam(required = false) String excludeUserId) {

        List<User> users = userService.findNearbyRescuers(latitude, longitude, radiusKm, excludeUserId);
        return ResponseEntity.ok(Map.of(
                "count", users.size(),
                "radiusKm", radiusKm,
                "users", users.stream().map(u -> Map.of(
                        "id", u.getId(),
                        "fullName", u.getFullName() != null ? u.getFullName() : u.getUsername(),
                        "phone", u.getPhone() != null ? u.getPhone() : "",
                        "latitude", u.getLatitude(),
                        "longitude", u.getLongitude(),
                        "isAvailableForRescue", u.isAvailableForRescue()
                )).toList()
        ));
    }

    @PutMapping("/availability")
    public ResponseEntity<?> toggleAvailability(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam boolean available) {

        User user = userService.toggleRescueAvailability(userId, available);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "message", "Availability updated",
                "isAvailableForRescue", user.isAvailableForRescue()
        ));
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {

        String token = body.get("fcmToken");
        User user = userService.updateFcmToken(userId, token);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "FCM token updated"));
    }
}