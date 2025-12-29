package com.example.hotel_room_allocation_system.infrastructure.http;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.limits")
public class ApiLimitsProperties {
    private long maxRequestSizeInBytes = 2_000_000;
    private int defaultExplainLimit = 1_000;
    private int maxExplainLimit = 5_000;

    public long getMaxRequestSizeInBytes() {
        return maxRequestSizeInBytes;
    }

    public void setMaxRequestSizeInBytes(long maxRequestSizeInBytes) {
        this.maxRequestSizeInBytes = maxRequestSizeInBytes;
    }

    public int getDefaultExplainLimit() {
        return defaultExplainLimit;
    }

    public void setDefaultExplainLimit(int defaultExplainLimit) {
        this.defaultExplainLimit = defaultExplainLimit;
    }

    public int getMaxExplainLimit() {
        return maxExplainLimit;
    }

    public void setMaxExplainLimit(int maxExplainLimit) {
        this.maxExplainLimit = maxExplainLimit;
    }
}
