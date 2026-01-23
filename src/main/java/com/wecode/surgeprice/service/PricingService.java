package com.wecode.surgeprice.service;

import com.wecode.surgeprice.dto.PriceResponseDTO;

public interface PricingService {

    PriceResponseDTO getPrice(double lat, double lng);

    double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2);

    double calculateSurge(long requestCount, long driverCount);

    double calculateBasePrice(double distanceKm);

    int selectResolution(double distanceKm);
}