package com.teamengineoil.usage_service.client;

import com.teamengineoil.usage_service.dto.DeviceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class DeviceClient {

    private final RestTemplate restTemplate;

    private final String baseUrl;
    @Value("${spring.security.user.name}")
    private String user;
    @Value("${spring.security.user.password}")
    private String password;

    public DeviceClient(@Value("${device.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public DeviceDto getDeviceById(Long deviceId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{deviceId}")
                .buildAndExpand(deviceId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(user, password);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<DeviceDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                DeviceDto.class
        );

        return response.getBody();
    }

    public List<DeviceDto> getAllDevicesForUser(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(user, password);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<DeviceDto[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                DeviceDto[].class
        );

        DeviceDto[] devices = response.getBody();

        return devices == null
                ? List.of()
                : List.of(devices);
    }
}
