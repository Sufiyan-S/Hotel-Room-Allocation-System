package com.example.hotel_room_allocation_system.infrastructure.idempotency;

import com.example.hotel_room_allocation_system.exception.IdempotencyConflictException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
public class InMemoryIdempotencyService implements IdempotencyService {

    private final Cache<String, IdempotencyEntry> caache;

    public InMemoryIdempotencyService(IdempotencyProperties properties) {
        long maxSize = Math.max(1, properties.getCache().getMaxSize());
        long ttlSeconds = Math.max(1, properties.getCache().getExpireAfterSeconds());

        this.caache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .build();
    }

    @Override
    public IdempotencyResult getOrCompute(@NonNull String idempotencyKey,
                                          @NonNull String requestHash,
                                          @NonNull Supplier<Object> responseSupplier) {
        AtomicBoolean replayed = new AtomicBoolean(false);

        IdempotencyEntry entry = caache.asMap().compute(idempotencyKey, (k, existingEntry) -> {
            if (existingEntry == null) {
                Object body = responseSupplier.get();
                return new IdempotencyEntry(requestHash, body);
            }

            replayed.set(true);

            if(!existingEntry.requestHash().equals(requestHash)){
                throw new IdempotencyConflictException("Idempotency key conflict detected for key: " + idempotencyKey);
            }

            return existingEntry;
        });

        return new IdempotencyResult(entry.responseBody(), replayed.get());
    }
}
