package com.example.navic.controllers;

import com.example.navic.dto.SOSRequest;
import com.example.navic.dto.SOSResponse;
import com.example.navic.models.SOSAlert;
import com.example.navic.services.SOSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sos")
public class SOSController {

    @Autowired
    private SOSService sosService;

    @PostMapping("/create")
    public ResponseEntity<?> createAlert(
            @RequestBody SOSRequest request,
            @RequestHeader("X-User-Id") String userId) {

        if (request.latitude() == 0 || request.longitude() == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required"));
        }

        SOSAlert alert = sosService.createAlert(request, userId);

        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Could not create SOS. You may have an active alert already."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "alertCode", alert.getAlertCode(),
                "alert", SOSResponse.fromEntity(alert)
        ));
    }

    @GetMapping("/my-alerts")
    public ResponseEntity<?> getMyAlerts(@RequestHeader("X-User-Id") String userId) {
        List<SOSAlert> alerts = sosService.getAlertsBySender(userId);
        return ResponseEntity.ok(Map.of(
                "count", alerts.size(),
                "alerts", alerts.stream().map(SOSResponse::fromEntity).collect(Collectors.toList())
        ));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAlerts(@RequestHeader("X-User-Id") String userId) {
        List<SOSAlert> alerts = sosService.getPendingAlertsForUser(userId);
        return ResponseEntity.ok(Map.of(
                "count", alerts.size(),
                "alerts", alerts.stream().map(SOSResponse::fromEntity).collect(Collectors.toList())
        ));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyAlerts(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radiusKm) {

        List<SOSAlert> alerts = sosService.getNearbyActiveAlerts(latitude, longitude, radiusKm);
        return ResponseEntity.ok(Map.of(
                "count", alerts.size(),
                "alerts", alerts.stream().map(SOSResponse::fromEntity).collect(Collectors.toList())
        ));
    }

    @PostMapping("/{alertId}/accept")
    public ResponseEntity<?> acceptAlert(
            @PathVariable String alertId,
            @RequestHeader("X-User-Id") String responderId) {

        SOSAlert alert = sosService.acceptAlert(alertId, responderId);
        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not accept alert"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alert accepted. Navigate to the victim.",
                "alert", SOSResponse.fromEntity(alert)
        ));
    }

    @PutMapping("/{alertId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String alertId,
            @RequestHeader("X-User-Id") String responderId,
            @RequestParam String status) {

        SOSAlert alert = sosService.updateResponderStatus(alertId, responderId, status);
        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not update status"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "alert", SOSResponse.fromEntity(alert)
        ));
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<?> resolveAlert(
            @PathVariable String alertId,
            @RequestHeader("X-User-Id") String responderId,
            @RequestBody Map<String, String> body) {

        String notes = body.get("notes");
        String resolutionType = body.getOrDefault("resolutionType", "RESCUED");

        SOSAlert alert = sosService.resolveAlert(alertId, responderId, notes, resolutionType);
        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not resolve alert"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alert resolved successfully",
                "alert", SOSResponse.fromEntity(alert)
        ));
    }

    @PostMapping("/{alertId}/cancel")
    public ResponseEntity<?> cancelAlert(
            @PathVariable String alertId,
            @RequestHeader("X-User-Id") String senderId) {

        SOSAlert alert = sosService.cancelAlert(alertId, senderId);
        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not cancel alert"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Alert cancelled"
        ));
    }

    @PutMapping("/{alertId}/location/sender")
    public ResponseEntity<?> updateSenderLocation(
            @PathVariable String alertId,
            @RequestHeader("X-User-Id") String senderId,
            @RequestBody Map<String, Object> body) {

        double lat = ((Number) body.get("latitude")).doubleValue();
        double lng = ((Number) body.get("longitude")).doubleValue();
        double accuracy = body.containsKey("accuracy") ? ((Number) body.get("accuracy")).doubleValue() : 0;
        String source = (String) body.getOrDefault("source", "GPS");

        SOSAlert alert = sosService.updateSenderLocation(alertId, senderId, lat, lng, accuracy, source);
        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not update location"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{alertId}/location/responder")
    public ResponseEntity<?> updateResponderLocation(
            @PathVariable String alertId,
            @RequestBody Map<String, Object> body) {

        double lat = ((Number) body.get("latitude")).doubleValue();
        double lng = ((Number) body.get("longitude")).doubleValue();
        double accuracy = body.containsKey("accuracy") ? ((Number) body.get("accuracy")).doubleValue() : 0;
        String source = (String) body.getOrDefault("source", "GPS");

        SOSAlert alert = sosService.updateResponderLocation(alertId, lat, lng, accuracy, source);
        if (alert == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not update location"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<?> getAlert(@PathVariable String alertId) {
        SOSAlert alert = sosService.getAlertById(alertId);
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "alert", SOSResponse.fromEntity(alert),
                "senderLocationHistory", alert.getSenderLocationHistory(),
                "responderLocationHistory", alert.getResponderLocationHistory(),
                "responders", alert.getResponders()
        ));
    }

    @GetMapping("/code/{alertCode}")
    public ResponseEntity<?> getAlertByCode(@PathVariable String alertCode) {
        SOSAlert alert = sosService.getAlertByCode(alertCode);
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(SOSResponse.fromEntity(alert));
    }

    @GetMapping("/my-rescues")
    public ResponseEntity<?> getMyRescues(@RequestHeader("X-User-Id") String userId) {
        List<SOSAlert> alerts = sosService.getAlertsAcceptedByResponder(userId);
        return ResponseEntity.ok(Map.of(
                "count", alerts.size(),
                "alerts", alerts.stream().map(SOSResponse::fromEntity).collect(Collectors.toList())
        ));
    }
}