package com.example.navic.services;

import com.example.navic.dto.SOSRequest;
import com.example.navic.models.SOSAlert;
import com.example.navic.models.User;
import com.example.navic.repositories.SOSAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SOSService {

    @Autowired
    private SOSAlertRepository sosAlertRepository;

    @Autowired
    private UserService userService;

    public SOSAlert createAlert(SOSRequest request, String senderId) {
        User sender = userService.getUserById(senderId);
        if (sender == null) return null;

        long activeCount = sosAlertRepository.countBySenderIdAndStatusIn(
                senderId,
                List.of("PENDING", "ALERTED", "ACCEPTED", "IN_PROGRESS")
        );
        if (activeCount > 0) {
            return null;
        }

        SOSAlert alert = new SOSAlert();
        alert.generateAlertCode();
        alert.setSenderId(senderId);
        alert.setSenderName(sender.getFullName() != null ? sender.getFullName() : sender.getUsername());
        alert.setSenderPhone(sender.getPhone());
        alert.setSenderEmergencyContact(sender.getEmergencyContact());
        alert.setSenderEmergencyContactPhone(sender.getEmergencyContactPhone());

        alert.setLatitude(request.latitude());
        alert.setLongitude(request.longitude());
        alert.setLocation(new GeoJsonPoint(request.longitude(), request.latitude()));
        alert.setAccuracy(request.accuracy());
        alert.setAddress(request.address());

        alert.setEmergencyType(request.emergencyType() != null ? request.emergencyType() : "OTHER");
        alert.setPriority(request.priority() != null ? request.priority() : "HIGH");
        alert.setDescription(request.description());
        alert.setCommunicationMode(request.communicationMode() != null ? request.communicationMode() : "INTERNET");

        if (request.images() != null) alert.setImages(request.images());
        alert.setAudioMessage(request.audioMessage());

        alert.addSenderLocation(request.latitude(), request.longitude(), request.accuracy(), "GPS");

        SOSAlert savedAlert = sosAlertRepository.save(alert);

        userService.incrementSosCount(senderId);

        notifyNearbyRescuers(savedAlert);

        return savedAlert;
    }

    private void notifyNearbyRescuers(SOSAlert alert) {
        List<User> nearbyUsers = userService.findNearbyRescuers(
                alert.getLatitude(),
                alert.getLongitude(),
                5.0,
                alert.getSenderId()
        );

        if (!nearbyUsers.isEmpty()) {
            for (User user : nearbyUsers) {
                double distance = calculateDistance(
                        alert.getLatitude(), alert.getLongitude(),
                        user.getLatitude(), user.getLongitude()
                );

                SOSAlert.Responder responder = new SOSAlert.Responder(
                        user.getId(),
                        user.getFullName() != null ? user.getFullName() : user.getUsername(),
                        user.getPhone(),
                        distance
                );
                alert.addResponder(responder);
            }

            alert.setStatus("ALERTED");
            alert.setAlertedAt(Instant.now());
            sosAlertRepository.save(alert);
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<SOSAlert> getPendingAlertsForUser(String userId) {
        return sosAlertRepository.findPendingAlertsForUser(userId);
    }

    public List<SOSAlert> getAlertsBySender(String senderId) {
        return sosAlertRepository.findBySenderIdOrderByCreatedAtDesc(senderId);
    }

    public SOSAlert acceptAlert(String alertId, String responderId) {
        SOSAlert alert = sosAlertRepository.findById(alertId).orElse(null);
        User responder = userService.getUserById(responderId);

        if (alert == null || responder == null) return null;
        if (!List.of("PENDING", "ALERTED").contains(alert.getStatus())) return null;

        String responderName = responder.getFullName() != null ? responder.getFullName() : responder.getUsername();
        alert.acceptByResponder(responderId, responderName);

        return sosAlertRepository.save(alert);
    }

    public SOSAlert updateResponderStatus(String alertId, String responderId, String status) {
        SOSAlert alert = sosAlertRepository.findById(alertId).orElse(null);
        if (alert == null) return null;

        if ("ARRIVED".equals(status)) {
            alert.setArrivedAt(Instant.now());
            alert.setStatus("IN_PROGRESS");
        }

        for (SOSAlert.Responder r : alert.getResponders()) {
            if (r.getUserId().equals(responderId)) {
                r.setStatus(status);
                break;
            }
        }

        return sosAlertRepository.save(alert);
    }

    public SOSAlert resolveAlert(String alertId, String responderId, String notes, String resolutionType) {
        SOSAlert alert = sosAlertRepository.findById(alertId).orElse(null);
        if (alert == null) return null;

        alert.resolve(notes, resolutionType);

        if (alert.getPrimaryResponderId() != null) {
            userService.incrementRescueCount(alert.getPrimaryResponderId());
        }

        return sosAlertRepository.save(alert);
    }

    public SOSAlert cancelAlert(String alertId, String senderId) {
        SOSAlert alert = sosAlertRepository.findById(alertId).orElse(null);
        if (alert == null || !alert.getSenderId().equals(senderId)) return null;
        if (List.of("RESOLVED", "CANCELLED").contains(alert.getStatus())) return null;

        alert.setStatus("CANCELLED");
        alert.setUpdatedAt(Instant.now());
        return sosAlertRepository.save(alert);
    }

    public SOSAlert updateSenderLocation(String alertId, String senderId, double lat, double lng, double accuracy, String source) {
        SOSAlert alert = sosAlertRepository.findById(alertId).orElse(null);
        if (alert == null || !alert.getSenderId().equals(senderId)) return null;
        if (List.of("RESOLVED", "CANCELLED").contains(alert.getStatus())) return null;

        alert.addSenderLocation(lat, lng, accuracy, source);
        return sosAlertRepository.save(alert);
    }

    public SOSAlert updateResponderLocation(String alertId, double lat, double lng, double accuracy, String source) {
        SOSAlert alert = sosAlertRepository.findById(alertId).orElse(null);
        if (alert == null) return null;

        alert.addResponderLocation(lat, lng, accuracy, source);
        return sosAlertRepository.save(alert);
    }

    public SOSAlert getAlertById(String alertId) {
        return sosAlertRepository.findById(alertId).orElse(null);
    }

    public SOSAlert getAlertByCode(String alertCode) {
        return sosAlertRepository.findByAlertCode(alertCode);
    }

    public List<SOSAlert> getActiveAlerts() {
        return sosAlertRepository.findActiveAlerts();
    }

    public List<SOSAlert> getNearbyActiveAlerts(double lat, double lng, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        return sosAlertRepository.findNearbyActiveAlerts(lng, lat, radiusMeters);
    }

    public List<SOSAlert> getAlertsAcceptedByResponder(String responderId) {
        return sosAlertRepository.findByPrimaryResponderIdOrderByCreatedAtDesc(responderId);
    }
}