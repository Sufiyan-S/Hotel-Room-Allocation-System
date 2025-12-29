package com.example.hotel_room_allocation_system.infrastructure.idempotency;

public record IdempotencyEntry(
        String requestHash,
        Object responseBody
) {
}
