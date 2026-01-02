package com.example.hotel_room_allocation_system.api;

import com.example.hotel_room_allocation_system.api.dto.OccupancyExplainResponse;
import com.example.hotel_room_allocation_system.api.dto.OccupancyRequest;
import com.example.hotel_room_allocation_system.api.dto.OccupancyResponse;
import com.example.hotel_room_allocation_system.application.RoomAllocationService;
import com.example.hotel_room_allocation_system.domain.AllocationExplainResult;
import com.example.hotel_room_allocation_system.domain.AllocationExplanation;
import com.example.hotel_room_allocation_system.domain.AllocationResult;
import com.example.hotel_room_allocation_system.infrastructure.http.ApiLimitsProperties;
import com.example.hotel_room_allocation_system.infrastructure.idempotency.IdempotencyResult;
import com.example.hotel_room_allocation_system.infrastructure.idempotency.IdempotencyService;
import com.example.hotel_room_allocation_system.infrastructure.idempotency.RequestHashingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Occupancy")
public class OccupancyController {
    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final RoomAllocationService roomAllocationService;
    private final IdempotencyService idempotencyService;
    private final RequestHashingService requestHashingService;
    private final ApiLimitsProperties apiLimitsProperties;

    public OccupancyController(
            RoomAllocationService roomAllocationService,
            IdempotencyService idempotencyService,
            RequestHashingService requestHashingService,
            ApiLimitsProperties apiLimitsProperties
    ) {
        this.roomAllocationService = roomAllocationService;
        this.idempotencyService = idempotencyService;
        this.requestHashingService = requestHashingService;
        this.apiLimitsProperties = apiLimitsProperties;
    }

    @Operation(summary = "Calculate room occupancy and revenue")
    @PostMapping(value = "/occupancy", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> occupancy(
            @Valid @RequestBody OccupancyRequest request,
            @Parameter(description = "if true, returns an extended response with allocation decision details")
            @RequestParam(name = "explain", defaultValue = "false") boolean explain,
            @Parameter(description  = "Maximum number of items returned per list in explain mode")
            @RequestParam(name = "explainLimit", required = false) Integer explainLimit,
            @Parameter(description = "Optional idempotency key to ensure request uniqueness")
            @RequestHeader(name = IDEMPOTENCY_HEADER, required = false) String idempotencyKey
            ) {
        String key = normalizeKey(idempotencyKey);
        int effectiveExplainLimit = explain ? resolveExplainLimit(explainLimit) : 0;

        if(key != null && !explain) {
            String requestHash = requestHashingService.hash(request, false);
            IdempotencyResult result = idempotencyService.getOrCompute(
                    key,
                    requestHash,
                    () -> computeResponse(request, false, 0)
            );

            return ResponseEntity.ok()
                    .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                    .body(result.responseBody());
        }

        return ResponseEntity.ok(computeResponse(request, explain, effectiveExplainLimit));
    }

    private Object computeResponse(OccupancyRequest request, boolean explain, int explainLimit){
        int premiumRoom = request.premiumRooms() == null ? 0 : request.premiumRooms();
        int economyRoom = request.economyRooms() == null ? 0 : request.economyRooms();

        if(!explain){
            AllocationResult result = roomAllocationService.allocate(
                    premiumRoom,
                    economyRoom,
                    request.potentialGuests()
            );
            return new OccupancyResponse(
                    result.usagePremium(),
                    result.revenuePremium(),
                    result.usageEconomy(),
                    result.revenueEconomy()
            );
        }

        AllocationExplainResult result = roomAllocationService.allocateExplain(
                premiumRoom,
                economyRoom,
                request.potentialGuests(),
                explainLimit
        );

        AllocationResult summary = result.summary();
        AllocationExplanation ex = result.explanation();

        return new OccupancyExplainResponse(
                summary.usagePremium(),
                summary.revenuePremium(),
                summary.usageEconomy(),
                summary.revenueEconomy(),
                new OccupancyExplainResponse.Explaination(
                        ex.premiumCandidates(),
                        ex.economyCandidates(),
                        ex.upgrades(),
                        ex.allocatedPremiumCount(),
                        ex.allocatedEconomyCount(),
                        ex.rejectedPremiumCount(),
                        ex.rejectedEconomyCount(),
                        ex.explainLimit(),
                        ex.upgradedEconomyGuests(),
                        ex.allocatedPremiumGuests(),
                        ex.allocatedEconomyGuests(),
                        ex.rejectedPremiumGuests(),
                        ex.rejectedEconomyGuests()
                )
        );
    }


    private int resolveExplainLimit(Integer explainLimit){
        int defaultLimit = Math.max(1, apiLimitsProperties.getDefaultExplainLimit());
        int maxLimit = Math.max(1, apiLimitsProperties.getMaxExplainLimit());

        int limit = (explainLimit == null) ? defaultLimit : explainLimit;
        if(limit < 0){
            throw new IllegalArgumentException("explainLimit must be non-negative");
        }

        if(maxLimit > 0 && limit > maxLimit){
            throw new IllegalArgumentException("explainLimit exceeds maximum allowed of " + maxLimit);
        }

        return limit;
    }

    private static String normalizeKey(String key){
        if(key == null){
            return null;
        }

        String trimmed = key.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
