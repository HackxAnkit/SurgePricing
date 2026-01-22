package com.wecode.surgeprice.service;


import com.wecode.surgeprice.config.SurgePricingProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class RedisService {

    private static final String DRIVER_KEY_PREFIX = "geofence:%s:drivers";
    private static final String REQUEST_KEY_PREFIX = "geofence:%s:requests";
    private static final String DEMAND_KEY_PREFIX = "geofence:%s:demand";
    private static final String BASELINE_KEY_PREFIX = "geofence:%s:baseline";
    private static final String SURGE_KEY_PREFIX = "geofence:%s:surge";
    private static final String LAST_UPDATE_KEY_PREFIX = "geofence:%s:last_update";

    private final RedisTemplate<String, String> redisTemplate;
    private final SurgePricingProperties properties;

    public RedisService(RedisTemplate<String, String> redisTemplate, SurgePricingProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    // Driver operations
    public void addDriver(String geofenceId, String driverId) {
        String key = String.format(DRIVER_KEY_PREFIX, geofenceId);
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, driverId, now);
        pruneOld(key, now);
        redisTemplate.expire(key, Duration.ofSeconds(properties.getDataFreshnessSeconds()));
        updateLastSeen(geofenceId);
    }

    public long getDriverCount(String geofenceId) {
        String key = String.format(DRIVER_KEY_PREFIX, geofenceId);
        long now = System.currentTimeMillis();
        Long count = redisTemplate.opsForZSet()
                .count(key, now - properties.getDataFreshnessSeconds() * 1000L, now);
        return count != null ? count : 0;
    }

    public Set<String> getDrivers(String geofenceId) {
        String key = String.format(DRIVER_KEY_PREFIX, geofenceId);
        long now = System.currentTimeMillis();
        return redisTemplate.opsForZSet()
                .rangeByScore(key, now - properties.getDataFreshnessSeconds() * 1000L, now);
    }

    public void addRideRequest(String geofenceId, String requestJson) {
        String key = String.format(REQUEST_KEY_PREFIX, geofenceId);
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, requestJson, now);
        pruneOld(key, now);
        redisTemplate.expire(key, Duration.ofSeconds(properties.getDataFreshnessSeconds()));
    }

    public long getRideRequestCount(String geofenceId) {
        String key = String.format(REQUEST_KEY_PREFIX, geofenceId);
        long now = System.currentTimeMillis();
        Long count = redisTemplate.opsForZSet()
                .count(key, now - properties.getDataFreshnessSeconds() * 1000L, now);
        return count != null ? count : 0;
    }

    public List<String> getActiveRideRequests(String geofenceId) {
        String key = String.format(REQUEST_KEY_PREFIX, geofenceId);
        long now = System.currentTimeMillis();
        Set<String> results = redisTemplate.opsForZSet()
                .rangeByScore(key, now - properties.getDataFreshnessSeconds() * 1000L, now);
        return results != null ? List.copyOf(results) : List.of();
    }

    // Demand operations
    public void incrementDemand(String geofenceId) {
        String key = String.format(DEMAND_KEY_PREFIX, geofenceId);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(properties.getDataFreshnessSeconds()));
    }

    public long getDemandCount(String geofenceId) {
        String key = String.format(DEMAND_KEY_PREFIX, geofenceId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    // Baseline operations
    public void updateBaseline(String geofenceId, double baseline) {
        String key = String.format(BASELINE_KEY_PREFIX, geofenceId);
        redisTemplate.opsForValue().set(key, String.valueOf(baseline));
    }

    public double getBaseline(String geofenceId) {
        String key = String.format(BASELINE_KEY_PREFIX, geofenceId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Double.parseDouble(value) : 0.0;
    }

    // Surge operations
    public void updateSurge(String geofenceId, double surge) {
        String key = String.format(SURGE_KEY_PREFIX, geofenceId);
        redisTemplate.opsForValue().set(key, String.valueOf(surge));
    }

    public double getSurge(String geofenceId) {
        String key = String.format(SURGE_KEY_PREFIX, geofenceId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Double.parseDouble(value) : properties.getBaseSurgeMultiplier();
    }

    // Last update tracking
    private void updateLastSeen(String geofenceId) {
        String key = String.format(LAST_UPDATE_KEY_PREFIX, geofenceId);
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()));
    }

    public long getLastUpdate(String geofenceId) {
        String key = String.format(LAST_UPDATE_KEY_PREFIX, geofenceId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0;
    }

    // Get all active geofences
    public Set<String> getActiveGeofences() {
        Set<String> keys = redisTemplate.keys("geofence:*:drivers");
        return keys != null ? keys : Set.of();
    }

    private void pruneOld(String key, long now) {
        long cutoff = now - (properties.getDataFreshnessSeconds() * 1000L);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutoff);
    }
}