package com.teamengineoil.device_service.mapper;

import com.teamengineoil.device_service.dto.DeviceDto;
import com.teamengineoil.device_service.dto.UpdateRequest;
import com.teamengineoil.device_service.entity.Device;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceMapper {
    private final ModelMapper modelMapper;

    public DeviceDto toDto(Device device) {
        return modelMapper.map(device, DeviceDto.class);
    }

    public Device toEntity(DeviceDto dto) {
        return modelMapper.map(dto, Device.class);
    }

    public void updateDeviceEntity(UpdateRequest deviceDTO, Device device) {
        modelMapper.map(deviceDTO, device);
    }
}
