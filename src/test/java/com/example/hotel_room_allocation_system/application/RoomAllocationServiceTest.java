package com.example.hotel_room_allocation_system.application;

import com.example.hotel_room_allocation_system.domain.AllocationExplainResult;
import com.example.hotel_room_allocation_system.domain.AllocationResult;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoomAllocationServiceTest {
    private final RoomAllocationService service = new RoomAllocationService(new SimpleMeterRegistry());

    private static final List<BigDecimal> GUESTS = List.of(
            BigDecimal.valueOf(23),
            BigDecimal.valueOf(45),
            BigDecimal.valueOf(155),
            BigDecimal.valueOf(374),
            BigDecimal.valueOf(22),
            BigDecimal.valueOf(99.99),
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(101),
            BigDecimal.valueOf(115),
            BigDecimal.valueOf(209)
    );

    @Test
    void testCase1_fromPdf() {
        AllocationResult result = service.allocate(3, 3, GUESTS);
        assertEquals(3, result.usagePremium());
        assertEquals(new BigDecimal("738"), result.revenuePremium());
        assertEquals(3, result.usageEconomy());
        assertEquals(new BigDecimal("167.99"), result.revenueEconomy());
    }

    @Test
    void testCase2_fromPdf() {
        AllocationResult result = service.allocate(7, 5, GUESTS);
        assertEquals(6, result.usagePremium());
        assertEquals(new BigDecimal("1054"), result.revenuePremium());
        assertEquals(4, result.usageEconomy());
        assertEquals(new BigDecimal("189.99"), result.revenueEconomy());
    }

    @Test
    void testCase3_noEconomyRooms() {
        AllocationResult result = service.allocate(2, 7, GUESTS);
        assertEquals(2, result.usagePremium());
        assertEquals(new BigDecimal("583"), result.revenuePremium());
        assertEquals(4, result.usageEconomy());
        assertEquals(new BigDecimal("189.99"), result.revenueEconomy());
    }

    @Test
    void upgradeHighestPayingEconomyGuestFirst_whenPremiumRoomsAvailableAndEconomyOverbooked() {
        AllocationResult result = service.allocate(7, 3, GUESTS);
        assertEquals(7, result.usagePremium());
        assertEquals(new BigDecimal("1153.99"), result.revenuePremium());
        assertEquals(3, result.usageEconomy());
        assertEquals(new BigDecimal("90"), result.revenueEconomy());
    }

    @Test
    void handleEmptyGuestList() {
        AllocationResult result = service.allocate(5, 5, List.of());
        assertEquals(0, result.usagePremium());
        assertEquals(BigDecimal.ZERO, result.revenuePremium());
        assertEquals(0, result.usageEconomy());
        assertEquals(BigDecimal.ZERO, result.revenueEconomy());
    }

    @Test
    void explainMode_includeUpgradesAndRejections() {
        AllocationExplainResult result = service.allocateExplain(7, 3, GUESTS, 1000);

        assertEquals(7, result.summary().usagePremium());
        assertEquals(new BigDecimal("1153.99"), result.summary().revenuePremium());
        assertEquals(3, result.summary().usageEconomy());
        assertEquals(new BigDecimal("90"), result.summary().revenueEconomy());

        assertEquals(6, result.explanation().premiumCandidates());
        assertEquals(4, result.explanation().economyCandidates());
        assertEquals(1, result.explanation().upgrades());
        assertEquals(List.of(new BigDecimal("99.99")), result.explanation().upgradedEconomyGuests());
        assertEquals(List.of(), result.explanation().rejectedPremiumGuests());
        assertEquals(List.of(), result.explanation().rejectedEconomyGuests());
    }

    @Test
    void explainMode_returnsRejectedGuests_whenRoomsInsufficient() {
        AllocationExplainResult result = service.allocateExplain(1, 1, GUESTS, 1000);

        assertEquals(1, result.summary().usagePremium());
        assertEquals(new BigDecimal("374"), result.summary().revenuePremium());
        assertEquals(1, result.summary().usageEconomy());
        assertEquals(new BigDecimal("99.99"), result.summary().revenueEconomy());

        assertEquals(List.of(new BigDecimal("209"), new BigDecimal("155"), new BigDecimal("115"),
                new BigDecimal("101"), new BigDecimal("100")), result.explanation().rejectedPremiumGuests());
        assertEquals(List.of(new BigDecimal("45"), new BigDecimal("23"), new BigDecimal("22")), result.explanation().rejectedEconomyGuests());
    }
}
