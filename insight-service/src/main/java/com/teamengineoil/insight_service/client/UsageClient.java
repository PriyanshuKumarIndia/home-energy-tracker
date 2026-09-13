package com.teamengineoil.insight_service.client;

import com.teamengineoil.insight_service.dto.UsageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UsageClient {

    private final RestTemplate restTemplate;

    private final String baseUrl;

    public UsageClient(@Value("${usage.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Value("${spring.security.user.name}")
    private String user;

    @Value("${spring.security.user.password}")
    private String password;

    public UsageDto getXDaysUsageForUser (Long userId, int days) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(user, password);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<UsageDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                UsageDto.class
        );

        return response.getBody();
    }
}