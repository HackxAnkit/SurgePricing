package com.wecode.surgeprice.service;

import java.util.List;
import java.util.Set;

public interface RedisService {

    void addDriver(int resolution, String geofenceId, String driverId);

    long getDriverCount(int resolution, String geofenceId);

    Set<String> getDrivers(int resolution, String geofenceId);

    void addRideRequest(int resolution, String geofenceId, String requestJson);

    long getRideRequestCount(int resolution, String geofenceId);

    List<String> getActiveRideRequests(int resolution, String geofenceId);

    void incrementDemand(int resolution, String geofenceId);

    long getDemandCount(int resolution, String geofenceId);

    void updateBaseline(int resolution, String geofenceId, double baseline);

    double getBaseline(int resolution, String geofenceId);

    void updateSurge(int resolution, String geofenceId, double surge);

    double getSurge(int resolution, String geofenceId);

    long getLastUpdate(int resolution, String geofenceId);

    Set<String> getActiveGeofences();
}