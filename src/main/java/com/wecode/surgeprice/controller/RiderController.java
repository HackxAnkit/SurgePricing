package com.wecode.surgeprice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecode.surgeprice.dto.RidePricingResponseDTO;
import com.wecode.surgeprice.dto.RideRequestDTO;
import com.wecode.surgeprice.dto.RideRequestRecordDTO;
import com.wecode.surgeprice.service.GeofenceService;
import com.wecode.surgeprice.service.PricingService;
import com.wecode.surgeprice.service.RedisService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rider")
public class RiderController {

    private static final Logger logger = LoggerFactory.getLogger(RiderController.class);

    private final PricingService pricingService;
    private final GeofenceService geofenceService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public RiderController(PricingService pricingService,
                           GeofenceService geofenceService,
                           RedisService redisService,
                           ObjectMapper objectMapper) {
        this.pricingService = pricingService;
        this.geofenceService = geofenceService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/book")
    public ResponseEntity<RidePricingResponseDTO> bookRide(@Valid @RequestBody RideRequestDTO request) {
        String geofenceId = geofenceService.getGeofenceId(request.getPickupLat(), request.getPickupLng());

        double distanceKm = pricingService.calculateDistanceKm(
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDropLat(),
                request.getDropLng()
        );
        double basePrice = pricingService.calculateBasePrice(distanceKm);

        long nearbyDrivers = redisService.getDriverCount(geofenceId);
        long requestCount = redisService.getRideRequestCount(geofenceId) + 1;

        double surgeMultiplier = pricingService.calculateSurge(requestCount, nearbyDrivers);
        double ratio = nearbyDrivers > 0 ? (double) requestCount / (double) nearbyDrivers : requestCount;
        double finalPrice = basePrice * surgeMultiplier;

        RideRequestRecordDTO record = new RideRequestRecordDTO(
                request.getRiderId(),
                request.getPickupLat(),
                request.getPickupLng(),
                request.getDropLat(),
                request.getDropLng(),
                distanceKm,
                basePrice,
                surgeMultiplier,
                finalPrice,
                geofenceId,
                request.getPickupName(),
                request.getDropName(),
                System.currentTimeMillis()
        );

        try {
            String payload = objectMapper.writeValueAsString(record);
            redisService.addRideRequest(geofenceId, payload);
        } catch (Exception e) {
            logger.error("Failed to store ride request", e);
        }

        RidePricingResponseDTO response = new RidePricingResponseDTO(
                request.getRiderId(),
                distanceKm,
                basePrice,
                surgeMultiplier,
                finalPrice,
                geofenceId,
                nearbyDrivers,
                requestCount,
                ratio,
                request.getPickupName(),
                request.getDropName()
        );

        return ResponseEntity.ok(response);
    }
}
