package com.example.hotel_room_allocation_system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "OccupancyExplainResponse")
public record OccupancyExplainResponse (
        @Schema(example = "6", description = "Number of premium rooms occupied")
        int usagePremium,

        @Schema(example = "1054", description = "Revenue from occupied premium rooms (EUR)")
        BigDecimal revenuePremium,

        @Schema(example = "3", description = "Number of economy rooms occupied")
        int usageEconomy,

        @Schema(example = "189.99", description = "Revenue from occupied economy rooms (EUR)")
        BigDecimal revenueEconomy,

        @Schema(description = "Debug information about the allocation process")
        Explaination explaination
) {
    @Schema(name = "OccupancyExplainResponse")
    public record Explaination(
            @Schema(example = "6", description = "Number of economy candidates (>=100)")
            int premiumCandidates,

            @Schema(example = "4", description = "Number of premium candidates (<100)")
            int economyCandidates,

            @Schema(example = "1", description = "How many economy candidates were upgraded to premium rooms")
            int upgrades,

            @Schema(example = "6", description = "Total number of premium guest allocated (may be > list size due to explainLimit)")
            int allocatedPremiumCount,

            @Schema(example = "3", description =  "Total number of economy guest allocated (may be > list size due to explainLimit)")
            int allocatedEconomyCount,

            @Schema(example = "0", description = "Number of premium rooms that were rejected due to lack of availability")
            int rejectedPremiumCount,

            @Schema(example = "1", description = "Number of economy rooms that were rejected due to lack of availability")
            int rejectedEconomyCount,

            @Schema(example = "1000", description = "The maximum number of guest entries that were considered for this explanation")
            int explainLimit,

            @Schema(example = "[99.99]", description = "Economy guests that were upgraded to premium rooms")
            List<BigDecimal> upgradeEconomyGuests,

            @Schema(example = "[375,209]", description = "Premium guests that were allocated to premium rooms")
            List<BigDecimal> allocatedPremiumGuests,

            @Schema(example = "[45.99,75.00,68.00]", description = "Economy guests that were allocated to economy rooms")
            List<BigDecimal> allocatedEconomyGuests,

            @Schema(example = "[150.00, 200.00]", description = "Economy guests that were rejected due to lack of availability")
            List<BigDecimal> rejectedPremiumGuests,

            @Schema(example = "[50.00]", description = "Economy guests that were rejected due to lack of availability")
            List<BigDecimal> rejectedEconomyGuests
    ) {}
}
