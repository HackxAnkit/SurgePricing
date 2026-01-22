package com.wecode.surgeprice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "surge")
public class SurgePricingProperties {

    private int h3Resolution = 8; // ~0.5 km^2 hexagons
    private int minDrivers = 5;
    private double maxSurgeMultiplier = 3.0;
    private double baseSurgeMultiplier = 1.0;
    private double maxSurgeJump = 0.2;
    private int baselineWindowSeconds = 600; // 10 minutes
    private int dataFreshnessSeconds = 30;
    private int warmupSeconds = 30;
    private double baseFare = 10.0;
    private double surgeDropThreshold = 0.5; // 50% drop triggers surge

    // Getters and Setters
    public int getH3Resolution() { return h3Resolution; }
    public void setH3Resolution(int h3Resolution) { this.h3Resolution = h3Resolution; }

    public int getMinDrivers() { return minDrivers; }
    public void setMinDrivers(int minDrivers) { this.minDrivers = minDrivers; }

    public double getMaxSurgeMultiplier() { return maxSurgeMultiplier; }
    public void setMaxSurgeMultiplier(double maxSurgeMultiplier) {
        this.maxSurgeMultiplier = maxSurgeMultiplier;
    }

    public double getBaseSurgeMultiplier() { return baseSurgeMultiplier; }
    public void setBaseSurgeMultiplier(double baseSurgeMultiplier) {
        this.baseSurgeMultiplier = baseSurgeMultiplier;
    }

    public double getMaxSurgeJump() { return maxSurgeJump; }
    public void setMaxSurgeJump(double maxSurgeJump) { this.maxSurgeJump = maxSurgeJump; }

    public int getBaselineWindowSeconds() { return baselineWindowSeconds; }
    public void setBaselineWindowSeconds(int baselineWindowSeconds) {
        this.baselineWindowSeconds = baselineWindowSeconds;
    }

    public int getDataFreshnessSeconds() { return dataFreshnessSeconds; }
    public void setDataFreshnessSeconds(int dataFreshnessSeconds) {
        this.dataFreshnessSeconds = dataFreshnessSeconds;
    }

    public int getWarmupSeconds() { return warmupSeconds; }
    public void setWarmupSeconds(int warmupSeconds) { this.warmupSeconds = warmupSeconds; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getSurgeDropThreshold() { return surgeDropThreshold; }
    public void setSurgeDropThreshold(double surgeDropThreshold) {
        this.surgeDropThreshold = surgeDropThreshold;
    }
}
