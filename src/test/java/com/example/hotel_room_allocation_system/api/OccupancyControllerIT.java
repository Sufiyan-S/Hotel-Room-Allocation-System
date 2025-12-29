package com.example.hotel_room_allocation_system.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OccupancyControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void returnsExpectedOccupancy_forSimpleInput() throws Exception {
        String json = """
                {
                    "premiumRooms": 7,
                    "economyRooms": 5,
                    "guests": [23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]
                }
                """;

        mockMvc.perform(post("/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.premium.occupiedRooms", is(6)))
                .andExpect(jsonPath("$.premium.revenue", is(1054.99)))
                .andExpect(jsonPath("$.economy.occupiedRooms", is(4)))
                .andExpect(jsonPath("$.economy.revenue", is(189.0)));
    }

    @Test
    void returnsProblemDetails_onMalformedJson() throws Exception {
        mockMvc.perform(post("/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title", is("Malformed JSON")));
    }

    @Test
    void returnsExplainResponse_whenExplainTrue() throws Exception {
        String json = """
                {
                    "premiumRooms": 7,
                    "economyRooms": 3,
                    "guests": [23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]
                }
                """;

        mockMvc.perform(post("/occupancy")
                        .param("explain", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.steps", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.finalAllocation.premium.occupiedRooms", is(1)))
                .andExpect(jsonPath("$.finalAllocation.premium.revenue", is(155.0)))
                .andExpect(jsonPath("$.finalAllocation.economy.occupiedRooms", is(1)))
                .andExpect(jsonPath("$.finalAllocation.economy.revenue", is(45.0)));
    }

    @Test
    void idempotency_returnSameResponseForSameKeyAndPayload() throws Exception {
        String json = """
                {
                    "premiumRooms": 7,
                    "economyRooms": 5,
                    "guests": [23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]
                }
                """;

        String idempotencyKey = "test-key-123";

        // First request
        mockMvc.perform(post("/occupancy")
                        .header(OccupancyController.IDEMPOTENCY_HEADER, idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Idempotency-Replayed"))
                .andExpect(jsonPath("$.premium.occupiedRooms", is(6)))
                .andExpect(jsonPath("$.premium.revenue", is(1054.99)))
                .andExpect(jsonPath("$.economy.occupiedRooms", is(4)))
                .andExpect(jsonPath("$.economy.revenue", is(189.0)));

        // Second request with same key and payload
        mockMvc.perform(post("/occupancy")
                        .header(OccupancyController.IDEMPOTENCY_HEADER, idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string("Idempotency-Replayed", "true"))
                .andExpect(jsonPath("$.premium.occupiedRooms", is(6)))
                .andExpect(jsonPath("$.premium.revenue", is(1054.99)))
                .andExpect(jsonPath("$.economy.occupiedRooms", is(4)))
                .andExpect(jsonPath("$.economy.revenue", is(189.0)));
    }

    @Test
    void idempotency_returnsConflict_whenSameKeyUsedWithDifferentPayload() throws Exception {
        String json1 = """
                {
                    "premiumRooms": 7,
                    "economyRooms": 5,
                    "guests": [23, 45, 155]
                }
                """;

        String json2 = """
                {
                    "premiumRooms": 3,
                    "economyRooms": 2,
                    "guests": [99, 199, 299]
                }
                """;

        String idempotencyKey = "test-key-456";

        // First request
        mockMvc.perform(post("/occupancy")
                        .header(OccupancyController.IDEMPOTENCY_HEADER, idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json1))
                .andExpect(status().isOk());

        // Second request with same key but different payload
        mockMvc.perform(post("/occupancy")
                        .header(OccupancyController.IDEMPOTENCY_HEADER, idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title", is("Idempotency Key Conflict")));
    }
}
