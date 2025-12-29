package com.example.hotel_room_allocation_system.infrastructure.idempotency;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idempotency")
public class IdempotencyProperties {

    private final Cache cache = new Cache();

    public Cache getCache() {
        return cache;
    }

    public static class Cache {
        private long maxSize = 10_000;
        private long expireAfterSeconds = 600;

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public long getExpireAfterSeconds() {
            return expireAfterSeconds;
        }

        public void setExpireAfterSeconds(long expireAfterSeconds) {
            this.expireAfterSeconds = expireAfterSeconds;
        }
    }
}
