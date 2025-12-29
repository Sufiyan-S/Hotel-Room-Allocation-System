package com.example.hotel_room_allocation_system.infrastructure.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI(
            @Value("${spring.application.name}") String appName,
            @Value("${build.version:}") String buildVersion
    ){
        String version = (buildVersion == null || buildVersion.isBlank()) ? "v1" : buildVersion;

        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Room Allocation API")
                        .description("API for allocating hotel rooms based on customer budgets.")
                        .version(version))
                .addTagsItem(new Tag().name("Occupancy").description("Operations related to room occupancy calculations"));
    }
}
