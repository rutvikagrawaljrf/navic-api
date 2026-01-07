package com.example.navic.dto;

import com.example.navic.models.SOSAlert;
import java.time.Instant;

public record SOSResponse(
        String id,
        String alertCode,
        String senderId,
        String senderName,
        String senderPhone,
        double latitude,
        double longitude,
        String address,
        String status,
        String priority,
        String emergencyType,
        String description,
        String primaryResponderName,
        int notifiedCount,
        int responderCount,
        Instant createdAt,
        Instant acceptedAt,
        String communicationMode
) {
    public static SOSResponse fromEntity(SOSAlert alert) {
        return new SOSResponse(
                alert.getId(),
                alert.getAlertCode(),
                alert.getSenderId(),
                alert.getSenderName(),
                alert.getSenderPhone(),
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getAddress(),
                alert.getStatus(),
                alert.getPriority(),
                alert.getEmergencyType(),
                alert.getDescription(),
                alert.getPrimaryResponderName(),
                alert.getNotifiedCount(),
                alert.getResponders() != null ? alert.getResponders().size() : 0,
                alert.getCreatedAt(),
                alert.getAcceptedAt(),
                alert.getCommunicationMode()
        );
    }
}