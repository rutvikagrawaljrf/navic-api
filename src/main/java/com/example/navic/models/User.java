package com.example.navic.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.time.Instant;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    // Personal Information
    private String fullName;

    @Indexed(unique = true, sparse = true)
    private String phone;

    private String email;
    private String emergencyContact;
    private String emergencyContactPhone;
    private String profileImage;

    // Location Data
    @GeoSpatialIndexed
    private GeoJsonPoint location;

    private double latitude;
    private double longitude;
    private String manualAddress;
    private String fetchedAddress;
    private String city;
    private String state;
    private String pincode;
    private String country;

    // Status & Settings
    private boolean isAvailableForRescue;
    private boolean isActive;
    private boolean isVerified;
    private String role;
    private double rescueRadiusKm;

    // Device & Communication
    private String deviceId;
    private String fcmToken;
    private String bluetoothMac;
    private String loraId;

    // Timestamps
    private Instant lastLocationUpdate;
    private Instant lastActiveAt;
    private Instant createdAt;
    private Instant updatedAt;

    // Statistics
    private int sosCount;
    private int rescueCount;

    // Constructor
    public User() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.isActive = true;
        this.isAvailableForRescue = true;
        this.isVerified = false;
        this.role = "USER";
        this.rescueRadiusKm = 5.0;
        this.sosCount = 0;
        this.rescueCount = 0;
        this.country = "India";
    }

    // Helper method to update location
    public void updateLocation(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
        this.location = new GeoJsonPoint(lng, lat);
        this.lastLocationUpdate = Instant.now();
        this.updatedAt = Instant.now();
    }

    // All Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public GeoJsonPoint getLocation() { return location; }
    public void setLocation(GeoJsonPoint location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getManualAddress() { return manualAddress; }
    public void setManualAddress(String manualAddress) { this.manualAddress = manualAddress; }

    public String getFetchedAddress() { return fetchedAddress; }
    public void setFetchedAddress(String fetchedAddress) { this.fetchedAddress = fetchedAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isAvailableForRescue() { return isAvailableForRescue; }
    public void setAvailableForRescue(boolean availableForRescue) { isAvailableForRescue = availableForRescue; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getRescueRadiusKm() { return rescueRadiusKm; }
    public void setRescueRadiusKm(double rescueRadiusKm) { this.rescueRadiusKm = rescueRadiusKm; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getBluetoothMac() { return bluetoothMac; }
    public void setBluetoothMac(String bluetoothMac) { this.bluetoothMac = bluetoothMac; }

    public String getLoraId() { return loraId; }
    public void setLoraId(String loraId) { this.loraId = loraId; }

    public Instant getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(Instant lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }

    public Instant getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(Instant lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public int getSosCount() { return sosCount; }
    public void setSosCount(int sosCount) { this.sosCount = sosCount; }

    public int getRescueCount() { return rescueCount; }
    public void setRescueCount(int rescueCount) { this.rescueCount = rescueCount; }

    public void incrementSosCount() { this.sosCount++; }
    public void incrementRescueCount() { this.rescueCount++; }
}