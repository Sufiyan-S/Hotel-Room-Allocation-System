package com.example.hotel_room_allocation_system.infrastructure.idempotency;

import java.util.function.Supplier;

public interface IdempotencyService {

    IdempotencyResult getOrCompute(String idempotencyKey, String requestHash, Supplier<Object> responseSupplier);
}
