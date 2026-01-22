package com.wecode.surgeprice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class DriverLocationDTO {

    @NotNull
    @JsonProperty("driverId")
    private String driverId;

    @NotNull
    @JsonProperty("lat")
    private Double lat;

    @NotNull
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("timestamp")
    private Long timestamp;

    public DriverLocationDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public DriverLocationDTO(String driverId, Double lat, Double lng) {
        this.driverId = driverId;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}