package com.example.hotel_room_allocation_system.domain;

import java.math.BigDecimal;

public record AllocationResult(
        int usagePremium,
        BigDecimal revenuePremium,
        int usageEconomy,
        BigDecimal revenueEconomy
) {
}
