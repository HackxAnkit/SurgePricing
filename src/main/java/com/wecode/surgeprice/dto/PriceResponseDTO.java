package com.wecode.surgeprice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class PriceResponseDTO {

    @JsonProperty("baseFare")
    private double baseFare;

    @JsonProperty("surgeMultiplier")
    private double surgeMultiplier;

    @JsonProperty("finalPrice")
    private double finalPrice;

    @JsonProperty("geofenceId")
    private String geofenceId;

    public PriceResponseDTO(double baseFare, double surgeMultiplier, String geofenceId) {
        this.baseFare = baseFare;
        this.surgeMultiplier = surgeMultiplier;
        this.finalPrice = baseFare * surgeMultiplier;
        this.geofenceId = geofenceId;
    }

    // Getters
    public double getBaseFare() { return baseFare; }
    public double getSurgeMultiplier() { return surgeMultiplier; }
    public double getFinalPrice() { return finalPrice; }
    public String getGeofenceId() { return geofenceId; }
}