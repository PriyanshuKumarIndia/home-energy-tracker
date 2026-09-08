package com.teamengineoil.usage_service.client;

import com.teamengineoil.usage_service.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(@Value("${user.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    @Value("${spring.security.user.name}")
    private String user;

    @Value("${spring.security.user.password}")
    private String password;

    public UserDto getUserById(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(user, password);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                UserDto.class
        );

        return response.getBody();
    }
}
