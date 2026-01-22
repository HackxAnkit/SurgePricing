package com.wecode.surgeprice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecode.surgeprice.dto.DriverAvailabilityResponseDTO;
import com.wecode.surgeprice.dto.RideRequestRecordDTO;
import com.wecode.surgeprice.service.GeofenceService;
import com.wecode.surgeprice.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/driver")
public class DriverAvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(DriverAvailabilityController.class);

    private final GeofenceService geofenceService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    public DriverAvailabilityController(GeofenceService geofenceService,
                                        RedisService redisService,
                                        ObjectMapper objectMapper) {
        this.geofenceService = geofenceService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/availability")
    public ResponseEntity<DriverAvailabilityResponseDTO> availability(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng) {
        String geofenceId = geofenceService.getGeofenceId(lat, lng);
        long nearbyDrivers = redisService.getDriverCount(geofenceId);

        List<String> rawRequests = redisService.getActiveRideRequests(geofenceId);
        List<RideRequestRecordDTO> requests = new ArrayList<>();
        for (String raw : rawRequests) {
            try {
                requests.add(objectMapper.readValue(raw, RideRequestRecordDTO.class));
            } catch (Exception e) {
                logger.warn("Failed to parse ride request payload", e);
            }
        }

        DriverAvailabilityResponseDTO response = new DriverAvailabilityResponseDTO(
                geofenceId,
                nearbyDrivers,
                requests
        );
        return ResponseEntity.ok(response);
    }
}
