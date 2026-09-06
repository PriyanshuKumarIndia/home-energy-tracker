package com.teamengineoil.ingestion_service.simulation;

import com.teamengineoil.ingestion_service.dto.EnergyUsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Random;

@Slf4j
@Component
public class ContinuousDataSimulator {
    private final RestTemplate restTemplate = new RestTemplate();

    private final Random random = new Random();

    @Value("${spring.security.user.name}")
    private String user;

    @Value("${spring.security.user.password}")
    private String password;

    @Value(("${simulation.requests-per-interval}"))
    private int requestsPerInterval;

    @Value("${ingestion.endpoint}")
    private String ingestionEndpoint;
    //    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        for (int i = 0; i < requestsPerInterval; i++) {
            EnergyUsageDto energyUsageDto = EnergyUsageDto.builder()
                    .deviceId(random.nextLong(1, 6))
                    .energyConsumed((double) Math.round(random.nextDouble(0.0, 10.0) * 100) / 100)
                    .timestamp(Instant.now())
                    .build();
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth(user, password);

                HttpEntity<EnergyUsageDto> request = new HttpEntity<>(energyUsageDto, headers);
                restTemplate.postForEntity(ingestionEndpoint, request, Void.class);
                log.info("Simulation requests sent to ingestion endpoint: {} with data: {}", ingestionEndpoint, energyUsageDto);
            } catch (Exception e) {
                log.error("Simulation requests sent to ingestion endpoint failed: {}", e.getMessage());
            }
        }

    }
}
