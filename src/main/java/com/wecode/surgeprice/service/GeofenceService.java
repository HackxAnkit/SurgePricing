package com.wecode.surgeprice.service;

public interface GeofenceService {

    String getGeofenceId(double lat, double lng);

    String getGeofenceId(double lat, double lng, int resolution);
}