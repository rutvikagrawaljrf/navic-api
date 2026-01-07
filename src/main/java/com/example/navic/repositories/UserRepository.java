package com.example.navic.repositories;

import com.example.navic.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
    User findByPhone(String phone);
    User findByEmail(String email);

    List<User> findByLocationNear(Point location, Distance distance);

    @Query("{ 'location': { $nearSphere: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'isAvailableForRescue': true, 'isActive': true, '_id': { $ne: ?3 } }")
    List<User> findNearbyAvailableRescuers(double longitude, double latitude, double maxDistanceInMeters, String excludeUserId);

    List<User> findByIsActiveTrue();

    List<User> findByRole(String role);

    List<User> findByIdIn(List<String> ids);
}