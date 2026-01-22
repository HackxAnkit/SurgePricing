package com.wecode.surgeprice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DriverAvailabilityResponseDTO {

    @JsonProperty("geofenceId")
    private String geofenceId;

    @JsonProperty("nearbyDrivers")
    private long nearbyDrivers;

    @JsonProperty("activeRequests")
    private List<RideRequestRecordDTO> activeRequests;

    public DriverAvailabilityResponseDTO(String geofenceId,
                                         long nearbyDrivers,
                                         List<RideRequestRecordDTO> activeRequests) {
        this.geofenceId = geofenceId;
        this.nearbyDrivers = nearbyDrivers;
        this.activeRequests = activeRequests;
    }

    public String getGeofenceId() { return geofenceId; }
    public long getNearbyDrivers() { return nearbyDrivers; }
    public List<RideRequestRecordDTO> getActiveRequests() { return activeRequests; }
}
