package com.example.navic.repositories;

import com.example.navic.models.SOSAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.time.Instant;

public interface SOSAlertRepository extends MongoRepository<SOSAlert, String> {

    SOSAlert findByAlertCode(String alertCode);

    List<SOSAlert> findBySenderIdOrderByCreatedAtDesc(String senderId);

    List<SOSAlert> findByStatusOrderByCreatedAtDesc(String status);

    @Query("{ 'status': { $in: ['PENDING', 'ALERTED', 'ACCEPTED', 'IN_PROGRESS'] } }")
    List<SOSAlert> findActiveAlerts();

    @Query("{ 'notifiedUserIds': ?0, 'status': { $in: ['PENDING', 'ALERTED'] } }")
    List<SOSAlert> findPendingAlertsForUser(String userId);

    List<SOSAlert> findByPrimaryResponderIdOrderByCreatedAtDesc(String responderId);

    List<SOSAlert> findByCreatedAtAfterOrderByCreatedAtDesc(Instant after);

    @Query("{ 'status': 'PENDING', 'expiresAt': { $lt: ?0 } }")
    List<SOSAlert> findExpiredAlerts(Instant now);

    @Query("{ 'location': { $nearSphere: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'status': { $in: ['PENDING', 'ALERTED', 'ACCEPTED'] } }")
    List<SOSAlert> findNearbyActiveAlerts(double longitude, double latitude, double maxDistanceInMeters);

    long countBySenderIdAndStatusIn(String senderId, List<String> statuses);
}