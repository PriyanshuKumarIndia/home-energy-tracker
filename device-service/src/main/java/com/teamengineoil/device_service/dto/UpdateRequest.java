package com.teamengineoil.device_service.dto;

import com.teamengineoil.device_service.DeviceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateRequest {
    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String location;
    private Long userId;
}