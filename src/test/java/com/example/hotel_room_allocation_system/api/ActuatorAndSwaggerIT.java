package com.example.hotel_room_allocation_system.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActuatorAndSwaggerIT {

    @LocalServerPort
    int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void actuatorHealth_isUp(){
        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":\"UP\""));
    }

    @Test
    void swaggerUi_isServed(){
        String url = "http://localhost:" + port + "/swagger-ui/index.html";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Swagger UI"));
    }
}
