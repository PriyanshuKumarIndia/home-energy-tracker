package com.teamengineoil.device_service.dto;

import com.teamengineoil.device_service.DeviceType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeviceDto {
    private Long id;

    @NotBlank(message = "Device name is mandatory.")
    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String location;
    private Long userId;
}