package com.wecode.surgeprice.controller;

import com.wecode.surgeprice.dto.PriceResponseDTO;
import com.wecode.surgeprice.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/price")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<PriceResponseDTO> getPrice(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng) {

        PriceResponseDTO price = pricingService.getPrice(lat, lng);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "pricing"));
    }
}