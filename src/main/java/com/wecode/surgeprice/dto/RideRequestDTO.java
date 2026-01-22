package com.wecode.surgeprice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class RideRequestDTO {

    @NotNull
    @JsonProperty("riderId")
    private String riderId;

    @NotNull
    @JsonProperty("pickupLat")
    private Double pickupLat;

    @NotNull
    @JsonProperty("pickupLng")
    private Double pickupLng;

    @NotNull
    @JsonProperty("dropLat")
    private Double dropLat;

    @NotNull
    @JsonProperty("dropLng")
    private Double dropLng;

    @JsonProperty("pickupName")
    private String pickupName;

    @JsonProperty("dropName")
    private String dropName;

    @JsonProperty("timestamp")
    private Long timestamp;

    public RideRequestDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }

    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double dropLng) { this.dropLng = dropLng; }

    public String getPickupName() { return pickupName; }
    public void setPickupName(String pickupName) { this.pickupName = pickupName; }

    public String getDropName() { return dropName; }
    public void setDropName(String dropName) { this.dropName = dropName; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
