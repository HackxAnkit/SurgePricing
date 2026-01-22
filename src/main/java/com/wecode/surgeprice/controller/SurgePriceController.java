package com.wecode.surgeprice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surge")
public class SurgePriceController {

    @GetMapping("/health")
    public String health() {
        return "Surge Price Service is running";
    }
}
