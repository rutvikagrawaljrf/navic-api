package com.example.navic.services;

import com.example.navic.dto.RegisterRequest;
import com.example.navic.models.User;
import com.example.navic.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            return null;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        return jwtService.generate(user.getId());
    }

    public Map<String, Object> registerFull(RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (userRepository.findByUsername(request.username()) != null) {
            result.put("success", false);
            result.put("error", "Username already exists");
            return result;
        }

        if (request.phone() != null && userRepository.findByPhone(request.phone()) != null) {
            result.put("success", false);
            result.put("error", "Phone number already registered");
            return result;
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setEmergencyContact(request.emergencyContact());
        user.setEmergencyContactPhone(request.emergencyContactPhone());
        user.setLatitude(request.latitude());
        user.setLongitude(request.longitude());
        user.setLocation(new GeoJsonPoint(request.longitude(), request.latitude()));
        user.setManualAddress(request.manualAddress());
        user.setFetchedAddress(request.fetchedAddress());
        user.setCity(request.city());
        user.setState(request.state());
        user.setPincode(request.pincode());
        user.setDeviceId(request.deviceId());
        user.setBluetoothMac(request.bluetoothMac());
        user.setLoraId(request.loraId());
        user.setLastLocationUpdate(Instant.now());
        user.setLastActiveAt(Instant.now());

        User savedUser = userRepository.save(user);

        result.put("success", true);
        result.put("token", jwtService.generate(savedUser.getId()));
        result.put("userId", savedUser.getId());
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("error", "User not found");
            return result;
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            result.put("success", false);
            result.put("error", "Invalid password");
            return result;
        }

        user.setLastActiveAt(Instant.now());
        userRepository.save(user);

        result.put("success", true);
        result.put("token", jwtService.generate(user.getId()));
        result.put("userId", user.getId());
        result.put("fullName", user.getFullName());
        result.put("role", user.getRole());
        return result;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateLocation(String userId, double latitude, double longitude) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.updateLocation(latitude, longitude);
            user.setLastActiveAt(Instant.now());
            return userRepository.save(user);
        }
        return null;
    }

    public List<User> findNearbyRescuers(double latitude, double longitude, double radiusKm, String excludeUserId) {
        double radiusMeters = radiusKm * 1000;
        return userRepository.findNearbyAvailableRescuers(longitude, latitude, radiusMeters, excludeUserId);
    }

    public User toggleRescueAvailability(String userId, boolean available) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setAvailableForRescue(available);
            user.setUpdatedAt(Instant.now());
            return userRepository.save(user);
        }
        return null;
    }

    public User updateFcmToken(String userId, String fcmToken) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setFcmToken(fcmToken);
            return userRepository.save(user);
        }
        return null;
    }

    public User updateProfile(String userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (updates.containsKey("fullName")) user.setFullName((String) updates.get("fullName"));
            if (updates.containsKey("email")) user.setEmail((String) updates.get("email"));
            if (updates.containsKey("emergencyContact")) user.setEmergencyContact((String) updates.get("emergencyContact"));
            if (updates.containsKey("emergencyContactPhone")) user.setEmergencyContactPhone((String) updates.get("emergencyContactPhone"));
            if (updates.containsKey("manualAddress")) user.setManualAddress((String) updates.get("manualAddress"));
            if (updates.containsKey("rescueRadiusKm")) user.setRescueRadiusKm(((Number) updates.get("rescueRadiusKm")).doubleValue());
            user.setUpdatedAt(Instant.now());
            return userRepository.save(user);
        }
        return null;
    }

    public List<User> getUsersByIds(List<String> userIds) {
        return userRepository.findByIdIn(userIds);
    }

    public void incrementSosCount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.incrementSosCount();
            userRepository.save(user);
        }
    }

    public void incrementRescueCount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.incrementRescueCount();
            userRepository.save(user);
        }
    }
}