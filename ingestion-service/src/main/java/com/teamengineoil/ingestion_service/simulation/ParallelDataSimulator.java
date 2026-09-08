package com.teamengineoil.ingestion_service.simulation;

import com.teamengineoil.ingestion_service.dto.EnergyUsageDto;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ParallelDataSimulator implements CommandLineRunner {
    private final ExecutorService executorService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    @Value("${simulation.parallel-threads}")
    private int parallelThreads;

    @Value("${spring.security.user.name}")
    private String user;

    @Value("${spring.security.user.password}")
    private String password;

    @Value("${simulation.requests-per-interval}")
    private int requestsPerInterval;

    @Value("${ingestion.endpoint}")
    private String ingestionEndpoint;

    public ParallelDataSimulator() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("ParallelDataSimulator started");
        ((ThreadPoolExecutor) executorService).setCorePoolSize(parallelThreads);
    }

    @Scheduled(fixedRateString = "${simulation.interval-ms}")
    public void sendMockData() {
        int batchSize = requestsPerInterval / parallelThreads;
        int remainder = requestsPerInterval % parallelThreads;

        for (int j = 0; j < parallelThreads; j++) {
            int requestsForThread = batchSize + (j < remainder ? 1 : 0);
            executorService.submit(() -> {
                for (int i = 0; i < requestsForThread; i++) {
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
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        log.info("ParallelDataSimulator shutdown");
    }
}
