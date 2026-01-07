package com.example.navic.dto;

import java.util.List;

public record SOSRequest(
        double latitude,
        double longitude,
        double accuracy,
        String address,
        String emergencyType,
        String priority,
        String description,
        String communicationMode,
        List<String> images,
        String audioMessage
) {}