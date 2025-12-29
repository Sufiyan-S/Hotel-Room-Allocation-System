package com.example.hotel_room_allocation_system.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"api.limits.maxRequestBytes=80"})
@AutoConfigureMockMvc
public class RequestSizeLimitIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void returns413WhenRequestSizeExceedsLimit() throws Exception {
        String json = """
                {
                    "premiumRooms": 7,
                    "economyRooms": 5,
                    "potentialGuests": [23, 45, 155, 374, 22, 99, 100, 101, 115, 209]
                }
                """;

        mockMvc.perform(post("/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("application/problem+json")
                        .content(json))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type", is("urn:problem:request-too-large")))
                .andExpect(jsonPath("$.title", is("Payload Too Large")));
    }
}
