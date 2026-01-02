package com.example.hotel_room_allocation_system.application;

import com.example.hotel_room_allocation_system.domain.AllocationExplainResult;
import com.example.hotel_room_allocation_system.domain.AllocationExplanation;
import com.example.hotel_room_allocation_system.domain.AllocationResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class RoomAllocationService {
    private static final BigDecimal PREMIUM_THRESHOLD = BigDecimal.valueOf(100);
    private static final Logger log = LoggerFactory.getLogger(RoomAllocationService.class);

    private final Counter requests;
    private final DistributionSummary potentialGuestCount;
    private final DistributionSummary updatesCount;
    private final DistributionSummary revenuePremium;
    private final DistributionSummary revenueEconomy;
    private final Timer allocationTimer;

    public RoomAllocationService(MeterRegistry meterRegistry) {
        this.requests = Counter.builder("allocation.requests")
                .description("Number of room allocation requests")
                .register(meterRegistry);

        this.potentialGuestCount = DistributionSummary.builder("allocation.potentialGuests.count")
                .description("Number of potential guests per request")
                .register(meterRegistry);

        this.updatesCount = DistributionSummary.builder("allocation.upgrades.count")
                .description("Economy to premium upgrades per request")
                .register(meterRegistry);

        this.revenuePremium = DistributionSummary.builder("allocation.revenue.premium")
                .baseUnit("eur")
                .description("Premium revenue per request")
                .register(meterRegistry);

        this.revenueEconomy = DistributionSummary.builder("allocation.revenue.economy")
                .baseUnit("eur")
                .description("Economy revenue per request")
                .register(meterRegistry);

        this.allocationTimer = Timer.builder("allocation.duration")
                .description("Time spent computing room allocations")
                .publishPercentileHistogram()
                .maximumExpectedValue(Duration.ofSeconds(2))
                .register(meterRegistry);
    }

    public AllocationResult allocate(int premiumRooms, int economyRooms, List<BigDecimal> potentialGuests) {
        return allocationTimer.record(() -> {
            validateInputs(premiumRooms, economyRooms, potentialGuests);

            requests.increment();
            potentialGuestCount.record(potentialGuests.size());

            Computation c = scanGuests(premiumRooms, economyRooms, potentialGuests, 0, true);
            AllocationResult summary = computeSummary(premiumRooms, economyRooms, c);

            updatesCount.record(c.upgrades());
            revenuePremium.record(summary.revenueEconomy().doubleValue());
            revenueEconomy.record(summary.revenueEconomy().doubleValue());

            return summary;
        });
    }

    public AllocationExplainResult allocateExplain(int premiumRooms,
                                                    int economyRooms,
                                                    List<BigDecimal> potentialGuests,
                                                    int explainLimit) {
        return allocationTimer.record(() -> {
            validateInputs(premiumRooms, economyRooms, potentialGuests);

            int effectiveExplainLimit = Math.max(0, explainLimit);

            requests.increment();
            potentialGuestCount.record(potentialGuests.size());

            Computation c = scanGuests(premiumRooms, economyRooms, potentialGuests, effectiveExplainLimit, true);
            AllocationResult summary = computeSummary(premiumRooms, economyRooms, c);

            AllocationExplanation explanation = buildExplaination(premiumRooms, economyRooms, effectiveExplainLimit, c);

            updatesCount.record(explanation.upgrades());
            revenuePremium.record(summary.revenueEconomy().doubleValue());
            revenueEconomy.record(summary.revenueEconomy().doubleValue());

            return new AllocationExplainResult(summary, explanation);
        });
    }

    private static void validateInputs(int premiumRooms, int economyRooms, List<BigDecimal> potentialGuests) {
        if (premiumRooms < 0 || economyRooms < 0) {
            throw new IllegalArgumentException("Number of premium rooms cannot be negative");
        }
        if (potentialGuests == null) {
            throw new IllegalArgumentException("Potential guests list cannot be null");
        }
    }

    private static Computation scanGuests(int premiumRooms,
                                          int economyRooms,
                                          List<BigDecimal> potentialGuests,
                                          int explainLimit,
                                          boolean explainMode) {

        int premiumTopK = explainMode
                ? clampTopK((long) premiumRooms + (long) explainLimit, potentialGuests.size())
                : clampTopK((long) premiumRooms, potentialGuests.size());

        int economyTopK = explainMode
                ? clampTopK((long) economyRooms + (long) premiumRooms + (long) explainLimit, potentialGuests.size())
                : clampTopK((long) economyRooms + (long) premiumRooms, potentialGuests.size());

        PriorityQueue<BigDecimal> premiumHeap = premiumTopK > 0 ? new PriorityQueue<>(premiumTopK) : null;
        PriorityQueue<BigDecimal> economyHeap = economyTopK > 0 ? new PriorityQueue<>(economyTopK) : null;

        int premiumCount = 0;
        int economyCount = 0;

        for (BigDecimal g : potentialGuests) {
            if(g == null){
                continue;
            }
            if (g.compareTo(PREMIUM_THRESHOLD) >= 0) {
                premiumCount++;
                offerTopK(premiumHeap, g, premiumTopK);
            } else {
                economyCount++;
                offerTopK(economyHeap, g, economyTopK);
            }
        }

        int directPremium = Math.min(premiumRooms, premiumCount);
        int freePremium = premiumRooms - directPremium;

        int upgrades = (freePremium > 0 && economyCount > economyRooms)
                ? Math.min(freePremium, economyCount - economyRooms)
                : 0;

        return new Computation(
                premiumCount,
                economyCount,
                upgrades,
                sortedDesc(premiumHeap),
                sortedDesc(economyHeap)
        );
    }

    private static AllocationResult computeSummary(int premiumRooms, int economyRooms, Computation computation) {
        int directPremium = Math.min(premiumRooms, computation.premiumCount());
        int freePremium = premiumRooms - directPremium;
        int upgrades = computation.upgrades();

        int usagePremium = directPremium + upgrades;
        int usageEconomy = Math.min(economyRooms, computation.economyCount);
        if(freePremium > 0 && computation.economyCount > economyRooms){
            usageEconomy = economyRooms;
        }

        List<BigDecimal> allocatedPremiumGuests = subListSafe(computation.premiumTop, 0, directPremium);

        List<BigDecimal> economyTop = computation.economyTop;

        List<BigDecimal> upgradedEconomy = subListSafe(economyTop, 0, upgrades);
        List<BigDecimal> allocatedEconomy = subListSafe(economyTop, upgrades, upgrades + usageEconomy);

        BigDecimal revenuePremium = sum(allocatedPremiumGuests).add(sum(upgradedEconomy));
        BigDecimal revenueEconomy = sum(allocatedEconomy);

        return new AllocationResult(usagePremium, revenuePremium, usageEconomy, revenueEconomy);
    }

    private static AllocationExplanation buildExplaination(int premiumRooms, int economyRooms, int explainLimit, Computation computation) {
        int directPremium = Math.min(premiumRooms, computation.premiumCount());
        int freeUpgrade = premiumRooms - directPremium;
        int upgrades = computation.upgrades();

        int usageEconomy = Math.min(economyRooms, computation.economyCount);
        if(freeUpgrade > 0 && computation.economyCount > economyRooms){
            usageEconomy = economyRooms;
        }

        int allocatedPremiumCount = directPremium;
        int allocatedEconomyCount = usageEconomy;
        int rejectedPremiumCount = Math.max(0, computation.premiumCount - directPremium);
        int rejectedEconomyCount = Math.max(0, computation.economyCount - upgrades - usageEconomy);

        List<BigDecimal> upgradedEconomyGuests = subListSafe(computation.economyTop, 0, Math.min(upgrades, explainLimit));
        List<BigDecimal> allocatedPremiumGuests = subListSafe(computation.premiumTop, 0, Math.min(directPremium, explainLimit));
        List<BigDecimal> allocatedEconomyGuests = subListSafe(computation.economyTop, upgrades, upgrades + Math.min(usageEconomy, explainLimit));

        List<BigDecimal> rejectedPremiumGuest = subListSafe(computation.premiumTop, directPremium, directPremium + explainLimit);

        int rejectedEconomyStart = upgrades + usageEconomy;
        List<BigDecimal> rejectedEconomyGuests = subListSafe(computation.economyTop, rejectedEconomyStart, rejectedEconomyStart + explainLimit);

        return new AllocationExplanation(
                computation.premiumCount,
                computation.economyCount,
                upgrades,
                allocatedPremiumCount,
                allocatedEconomyCount,
                rejectedPremiumCount,
                rejectedPremiumCount,
                explainLimit,
                List.copyOf(upgradedEconomyGuests),
                List.copyOf(allocatedPremiumGuests),
                List.copyOf(allocatedEconomyGuests),
                List.copyOf(rejectedPremiumGuest),
                List.copyOf(rejectedEconomyGuests)
        );
    }


    private static void offerTopK(PriorityQueue<BigDecimal> minHeap, BigDecimal value, int k){
        if (minHeap == null || k <= 0) {
            return;
        }

        if(minHeap.size() < k) {
            minHeap.offer(value);
            return;
        }

        BigDecimal smallest = minHeap.peek();
        if(smallest != null && value.compareTo(smallest) > 0) {
            minHeap.poll();
            minHeap.offer(value);
        }
    }

    private static int clampTopK(long desired, int maxAvailable){
        if(desired <= 0) {
            return 0;
        }

        long clamped = Math.min(desired, (long) maxAvailable);
        return (int)Math.min(clamped, Integer.MAX_VALUE);
    }

    private static List<BigDecimal> sortedDesc(PriorityQueue<BigDecimal> heap){
        if(heap == null || heap.isEmpty()) {
            return List.of();
        }

        List<BigDecimal> list = new ArrayList<>(heap);
        list.sort(Comparator.reverseOrder());

        return List.copyOf(list);
    }

    private static BigDecimal sum(List<BigDecimal> values){
        if(values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            total = total.add(value);
        }

        return  total;
    }

    private static List<BigDecimal> subListSafe(List<BigDecimal> list, int fromInclusive, int toExclusive) {
        if(list == null || list.isEmpty() || fromInclusive >= toExclusive) {
            return List.of();
        }

        int from = Math.max(0, fromInclusive);
        int to = Math.min(list.size(), toExclusive);

        if(from >= to) {
            return List.of();
        }

        return list.subList(from, to);
    }

    private record Computation(
            int premiumCount,
            int economyCount,
            int upgrades,
            List<BigDecimal> premiumTop,
            List<BigDecimal> economyTop
    ) { }
}
