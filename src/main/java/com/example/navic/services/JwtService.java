package com.example.navic.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlDays;

    public JwtService(
            @Value("${security.jwt.ttl-days:7}") long ttlDays
    ) {
        // Generate a secure key for HS256
        this.key = Keys.hmacShaKeyFor(
                "NavICEmergencyResponseSystemSecretKeyForJWT2024ISRO".getBytes()
        );
        this.ttlDays = ttlDays;
    }

    /**
     * Generate JWT token for a user
     * @param subject - usually the user ID
     * @return JWT token string
     */
    public String generate(String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlDays * 24 * 60 * 60);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Validate and parse JWT token
     * @param token - JWT token string
     * @return subject (user ID) if valid, null otherwise
     */
    public String validate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if token is expired
     * @param token - JWT token string
     * @return true if expired, false otherwise
     */
    public boolean isExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get expiration time in seconds
     * @return expiration time in seconds
     */
    public long getExpirationSeconds() {
        return ttlDays * 24 * 60 * 60;
    }
}