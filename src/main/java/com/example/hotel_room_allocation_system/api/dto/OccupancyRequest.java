package com.example.hotel_room_allocation_system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "OccupancyRequest")
public record OccupancyRequest (
        @Schema(example = "7", description = "Number of available premium rooms")
        @Min(value = 0, message = "Premium rooms must be >= 0")
        @Max(value = 1_00_000, message = "Premium rooms must be <= 100000")
        Integer premiumRooms,

        @Schema(example = "5", description = "Number of available economy rooms")
        @Min(value = 0, message = "Economy rooms must be >= 0")
        @Max(value = 1_00_000, message = "Economy rooms must be <= 100000")
        Integer economyRooms,

        @Schema(example = "[23.00,45.99,155.00]",
                description = "List of potential guests with their offered prices (EUR)")
        @NotNull(message = "Potential guest prices list must not be null")
        @Size(max = 1_00_000, message = "Potential guest prices list size must be between 0 and 100000")
        List<@NotNull(message = "Potential guest prices must not be null")
                @DecimalMin(value = "0.0", inclusive = true, message = "Potential guest prices must be >= 0")
                @DecimalMax(value = "100000.0", inclusive = true, message = "Potential guest prices must be <= 100000")
                @Digits(integer = 7, fraction = 2, message = "Potential guest prices must have up to 7 integer digits and up to 2 fractional digits")
                BigDecimal> potentialGuests
) {
}
