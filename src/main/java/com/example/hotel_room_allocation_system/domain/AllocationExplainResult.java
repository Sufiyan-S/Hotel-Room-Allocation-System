package com.example.hotel_room_allocation_system.domain;

public record AllocationExplainResult(
        AllocationResult summary,
        AllocationExplanation explanation
) {
}
