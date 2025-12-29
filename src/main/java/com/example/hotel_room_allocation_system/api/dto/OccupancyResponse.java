package com.example.hotel_room_allocation_system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "OccupancyResponse")
public record OccupancyResponse (
        @Schema(example = "6", description = "Number of occupied premium rooms")
        int usagePremium,

        @Schema(example = "1054", description = "Revenue from occupied premium rooms (EUR)")
        BigDecimal revenuePremium,

        @Schema(example = "3", description = "Number of occupied economy rooms")
        int usageEconomy,

        @Schema(example = "189.99", description = "Revenue from occupied economy rooms (EUR)")
        BigDecimal revenueEconomy
) {
}
