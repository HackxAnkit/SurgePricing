package com.wecode.surgeprice.service;


import com.wecode.surgeprice.config.SurgePricingProperties;
import com.wecode.surgeprice.dto.PriceResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingService.class);

    private final GeofenceService geofenceService;
    private final RedisService redisService;
    private final SurgePricingProperties properties;

    public PricingService(GeofenceService geofenceService,
                          RedisService redisService,
                          SurgePricingProperties properties) {
        this.geofenceService = geofenceService;
        this.redisService = redisService;
        this.properties = properties;
    }

    /**
     * O(1) price lookup - just Redis get operations
     */
    public PriceResponseDTO getPrice(double lat, double lng) {
        // Convert to geofence (O(1) H3 operation)
        String geofenceId = geofenceService.getGeofenceId(lat, lng);

        // O(1) Redis lookup
        double surgeMultiplier = redisService.getSurge(geofenceId);

        // Calculate final price
        return new PriceResponseDTO(
                properties.getBaseFare(),
                surgeMultiplier,
                geofenceId
        );
    }
}