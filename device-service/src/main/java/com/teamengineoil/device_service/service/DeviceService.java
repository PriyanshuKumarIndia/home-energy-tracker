package com.teamengineoil.device_service.service;

import com.teamengineoil.device_service.dto.DeviceDto;
import com.teamengineoil.device_service.dto.UpdateRequest;
import com.teamengineoil.device_service.entity.Device;
import com.teamengineoil.device_service.exception.HomeEnergyTrackerException;
import com.teamengineoil.device_service.mapper.DeviceMapper;
import com.teamengineoil.device_service.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new HomeEnergyTrackerException("Device Not Found"));
        return deviceMapper.toDto(device);
    }

    public DeviceDto createDevice(DeviceDto dto) {
        Device device = deviceMapper.toEntity(dto);
        return deviceMapper.toDto(deviceRepository.save(device));
    }

    public void updateDevice(Long id, UpdateRequest dto) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new HomeEnergyTrackerException("Device Not Found"));
        deviceMapper.updateDeviceEntity(dto, device);
        deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new HomeEnergyTrackerException("Device Not Found"));
        deviceRepository.delete(device);
    }
}
