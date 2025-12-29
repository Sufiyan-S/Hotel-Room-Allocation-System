package com.example.hotel_room_allocation_system.infrastructure.idempotency;

public record IdempotencyResult(Object responseBody, boolean replayed) {
}
