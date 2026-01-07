package com.example.navic.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "sos_alerts")
public class SOSAlert {
    @Id
    private String id;

    private String alertCode;

    // Sender Information
    private String senderId;
    private String senderName;
    private String senderPhone;
    private String senderEmergencyContact;
    private String senderEmergencyContactPhone;

    // Location
    @GeoSpatialIndexed
    private GeoJsonPoint location;
    private double latitude;
    private double longitude;
    private String address;
    private double accuracy;

    // Alert Details
    private String status;
    private String priority;
    private String emergencyType;
    private String description;
    private String communicationMode;

    // Media
    private List<String> images;
    private String audioMessage;

    // Responders
    private String primaryResponderId;
    private String primaryResponderName;
    private List<Responder> responders;
    private List<String> notifiedUserIds;
    private int notifiedCount;

    // Timestamps
    private Instant createdAt;
    private Instant alertedAt;
    private Instant acceptedAt;
    private Instant arrivedAt;
    private Instant resolvedAt;
    private Instant updatedAt;
    private Instant expiresAt;

    // Live Location Tracking
    private List<LocationUpdate> senderLocationHistory;
    private List<LocationUpdate> responderLocationHistory;

    // Resolution
    private String resolutionNotes;
    private String resolutionType;
    private int responseTimeMinutes;

    // Inner Classes
    public static class Responder {
        private String userId;
        private String name;
        private String phone;
        private String status;
        private double distanceKm;
        private Instant notifiedAt;
        private Instant respondedAt;

        public Responder() {}

        public Responder(String userId, String name, String phone, double distanceKm) {
            this.userId = userId;
            this.name = name;
            this.phone = phone;
            this.distanceKm = distanceKm;
            this.status = "NOTIFIED";
            this.notifiedAt = Instant.now();
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        public Instant getNotifiedAt() { return notifiedAt; }
        public void setNotifiedAt(Instant notifiedAt) { this.notifiedAt = notifiedAt; }
        public Instant getRespondedAt() { return respondedAt; }
        public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
    }

    public static class LocationUpdate {
        private double latitude;
        private double longitude;
        private double accuracy;
        private Instant timestamp;
        private String source;

        public LocationUpdate() {}

        public LocationUpdate(double lat, double lng, double accuracy, String source) {
            this.latitude = lat;
            this.longitude = lng;
            this.accuracy = accuracy;
            this.timestamp = Instant.now();
            this.source = source;
        }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    // Constructor
    public SOSAlert() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = "PENDING";
        this.priority = "HIGH";
        this.emergencyType = "OTHER";
        this.communicationMode = "INTERNET";
        this.responders = new ArrayList<>();
        this.notifiedUserIds = new ArrayList<>();
        this.senderLocationHistory = new ArrayList<>();
        this.responderLocationHistory = new ArrayList<>();
        this.images = new ArrayList<>();
        this.notifiedCount = 0;
        this.expiresAt = Instant.now().plusSeconds(3600);
    }

    public void generateAlertCode() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        this.alertCode = "SOS-" + java.time.Year.now().getValue() + "-" + timestamp;
    }

    public void addSenderLocation(double lat, double lng, double accuracy, String source) {
        this.latitude = lat;
        this.longitude = lng;
        this.location = new GeoJsonPoint(lng, lat);
        this.accuracy = accuracy;
        this.senderLocationHistory.add(new LocationUpdate(lat, lng, accuracy, source));
        this.updatedAt = Instant.now();
    }

    public void addResponderLocation(double lat, double lng, double accuracy, String source) {
        this.responderLocationHistory.add(new LocationUpdate(lat, lng, accuracy, source));
        this.updatedAt = Instant.now();
    }

    public void addResponder(Responder responder) {
        this.responders.add(responder);
        this.notifiedUserIds.add(responder.getUserId());
        this.notifiedCount++;
    }

    public void acceptByResponder(String userId, String name) {
        this.primaryResponderId = userId;
        this.primaryResponderName = name;
        this.status = "ACCEPTED";
        this.acceptedAt = Instant.now();
        this.updatedAt = Instant.now();

        for (Responder r : responders) {
            if (r.getUserId().equals(userId)) {
                r.setStatus("ACCEPTED");
                r.setRespondedAt(Instant.now());
                break;
            }
        }
    }

    public void resolve(String notes, String resolutionType) {
        this.status = "RESOLVED";
        this.resolutionNotes = notes;
        this.resolutionType = resolutionType;
        this.resolvedAt = Instant.now();
        this.updatedAt = Instant.now();

        if (this.acceptedAt != null) {
            long minutes = java.time.Duration.between(this.acceptedAt, this.resolvedAt).toMinutes();
            this.responseTimeMinutes = (int) minutes;
        }
    }

    // All Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAlertCode() { return alertCode; }
    public void setAlertCode(String alertCode) { this.alertCode = alertCode; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public String getSenderEmergencyContact() { return senderEmergencyContact; }
    public void setSenderEmergencyContact(String senderEmergencyContact) { this.senderEmergencyContact = senderEmergencyContact; }

    public String getSenderEmergencyContactPhone() { return senderEmergencyContactPhone; }
    public void setSenderEmergencyContactPhone(String senderEmergencyContactPhone) { this.senderEmergencyContactPhone = senderEmergencyContactPhone; }

    public GeoJsonPoint getLocation() { return location; }
    public void setLocation(GeoJsonPoint location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; this.updatedAt = Instant.now(); }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCommunicationMode() { return communicationMode; }
    public void setCommunicationMode(String communicationMode) { this.communicationMode = communicationMode; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getAudioMessage() { return audioMessage; }
    public void setAudioMessage(String audioMessage) { this.audioMessage = audioMessage; }

    public String getPrimaryResponderId() { return primaryResponderId; }
    public void setPrimaryResponderId(String primaryResponderId) { this.primaryResponderId = primaryResponderId; }

    public String getPrimaryResponderName() { return primaryResponderName; }
    public void setPrimaryResponderName(String primaryResponderName) { this.primaryResponderName = primaryResponderName; }

    public List<Responder> getResponders() { return responders; }
    public void setResponders(List<Responder> responders) { this.responders = responders; }

    public List<String> getNotifiedUserIds() { return notifiedUserIds; }
    public void setNotifiedUserIds(List<String> notifiedUserIds) { this.notifiedUserIds = notifiedUserIds; }

    public int getNotifiedCount() { return notifiedCount; }
    public void setNotifiedCount(int notifiedCount) { this.notifiedCount = notifiedCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getAlertedAt() { return alertedAt; }
    public void setAlertedAt(Instant alertedAt) { this.alertedAt = alertedAt; }

    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }

    public Instant getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(Instant arrivedAt) { this.arrivedAt = arrivedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public List<LocationUpdate> getSenderLocationHistory() { return senderLocationHistory; }
    public void setSenderLocationHistory(List<LocationUpdate> senderLocationHistory) { this.senderLocationHistory = senderLocationHistory; }

    public List<LocationUpdate> getResponderLocationHistory() { return responderLocationHistory; }
    public void setResponderLocationHistory(List<LocationUpdate> responderLocationHistory) { this.responderLocationHistory = responderLocationHistory; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolutionType() { return resolutionType; }
    public void setResolutionType(String resolutionType) { this.resolutionType = resolutionType; }

    public int getResponseTimeMinutes() { return responseTimeMinutes; }
    public void setResponseTimeMinutes(int responseTimeMinutes) { this.responseTimeMinutes = responseTimeMinutes; }
}