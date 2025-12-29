package com.example.hotel_room_allocation_system.api;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiContractTest {

    @LocalServerPort
    int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void openApiContract_isGeneratedAndParseable() {
        String url = "http://localhost:" + port + "/v3/api-docs";
        String openApiJson = this.restTemplate.getForObject(url, String.class);
        assertNotNull(openApiJson);

        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openApiJson, null, null);
        assertNotNull(parseResult.getOpenAPI(), "Expected OpenAPI definition to be parseable");

        if(parseResult.getMessages() != null && !parseResult.getMessages().isEmpty()) {
            fail("OpenAPI parsing errors: " + String.join(", ", parseResult.getMessages()));
        }

        assertTrue(parseResult.getOpenAPI().getPaths().containsKey("/occupancy"), "Expected /occupancy path to be present in OpenAPI definition");
    }
}
