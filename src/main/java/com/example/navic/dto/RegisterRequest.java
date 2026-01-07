package com.example.navic.dto;

public record RegisterRequest(
        String username,
        String password,
        String fullName,
        String phone,
        String email,
        String emergencyContact,
        String emergencyContactPhone,
        double latitude,
        double longitude,
        String manualAddress,
        String fetchedAddress,
        String city,
        String state,
        String pincode,
        String deviceId,
        String bluetoothMac,
        String loraId
) {}