package com.example.hotel_room_allocation_system;

import com.example.hotel_room_allocation_system.infrastructure.http.ApiLimitsProperties;
import com.example.hotel_room_allocation_system.infrastructure.idempotency.IdempotencyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IdempotencyProperties.class, ApiLimitsProperties.class})
public class BackendEngineerCodingChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendEngineerCodingChallengeApplication.class, args);
	}
}
