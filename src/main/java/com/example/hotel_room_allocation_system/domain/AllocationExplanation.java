package com.example.hotel_room_allocation_system.domain;

import java.math.BigDecimal;
import java.util.List;

public record AllocationExplanation(
        int premiumCandidates,
        int economyCandidates,
        int upgrades,
        int allocatedPremiumCount,
        int allocatedEconomyCount,
        int rejectedPremiumCount,
        int rejectedEconomyCount,
        int explainLimit,
        List<BigDecimal> upgradedEconomyGuests,
        List<BigDecimal> allocatedPremiumGuests,
        List<BigDecimal> allocatedEconomyGuests,
        List<BigDecimal> rejectedPremiumGuests,
        List<BigDecimal> rejectedEconomyGuests
) {
}
