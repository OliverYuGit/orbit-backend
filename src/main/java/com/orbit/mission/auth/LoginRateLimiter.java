package com.orbit.mission.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for login attempts.
 * Allows up to MAX_ATTEMPTS per WINDOW_SECONDS per (IP + username) key.
 * For production, replace with Redis-backed bucket4j for multi-instance support.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private record Bucket(AtomicInteger count, long windowStart) {}

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip, String username) {
        String key = ip + "|" + username.toLowerCase();
        long now = Instant.now().getEpochSecond();

        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_SECONDS) {
                return new Bucket(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        return bucket.count().get() <= MAX_ATTEMPTS;
    }

    public void reset(String ip, String username) {
        buckets.remove(ip + "|" + username.toLowerCase());
    }
}
