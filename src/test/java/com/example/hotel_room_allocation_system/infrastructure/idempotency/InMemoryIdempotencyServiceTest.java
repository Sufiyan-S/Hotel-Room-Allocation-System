package com.example.hotel_room_allocation_system.infrastructure.idempotency;

import com.example.hotel_room_allocation_system.exception.IdempotencyConflictException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryIdempotencyServiceTest {

    @Test
    void getOrCompute_isSingleFlightPerKey() throws Exception{
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.getCache().setMaxSize(100);
        properties.getCache().setExpireAfterSeconds(60);

        InMemoryIdempotencyService service = new InMemoryIdempotencyService(properties);

        AtomicInteger supplierCalls = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<Object>> futures = new ArrayList<>();

            for(int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> service.getOrCompute(
                        "same-key",
                        "same-hash",
                        () -> {
                            supplierCalls.incrementAndGet();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "response";
                        }
                ).responseBody()));
            }

            for(Future<Object> f : futures){
                assertEquals("response", f.get(3, TimeUnit.SECONDS));
            }

            assertEquals(1, supplierCalls.get(), "Supplier should be called only once for the same key");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void getOrCompute_throwConflict_whenSameKeyReusedWithDifferentHash() {
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.getCache().setMaxSize(100);
        properties.getCache().setExpireAfterSeconds(60);

        InMemoryIdempotencyService service = new InMemoryIdempotencyService(properties);

        service.getOrCompute("key", "hash1", () -> "response1");

        assertThrows(IdempotencyConflictException.class,
                () -> service.getOrCompute("key", "hash2", () -> "response2"));
    }
}
