package com.teamengineoil.device_service.controller;

import com.teamengineoil.device_service.dto.DeviceDto;
import com.teamengineoil.device_service.dto.UpdateRequest;
import com.teamengineoil.device_service.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody DeviceDto dto) {
        DeviceDto device = deviceService.createDevice(dto);
        return new ResponseEntity<>(device, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDevice(@PathVariable Long id, @Valid @RequestBody UpdateRequest dto) {
        deviceService.updateDevice(id, dto);
        return ResponseEntity.ok("Device updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok("Device deleted successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDto>> getDeviceByUserId(@PathVariable Long userId) {
        List<DeviceDto> devices = deviceService.getAllDevicesForUserId(userId);
        return ResponseEntity.ok(devices);
    }
}
