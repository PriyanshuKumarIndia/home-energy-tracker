package com.teamengineoil.device_service;

import com.teamengineoil.device_service.entity.Device;
import com.teamengineoil.device_service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {
	public static final int numberOfUsers = 10;
	@Autowired
	private DeviceRepository deviceRepository;

	private static final int numberOfDevices = 200;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createDevices() {
		for (int i = 1; i <= numberOfDevices; i++) {
			var device = Device.builder()
					.name("Device" + i)
					.type(DeviceType.values()[i % DeviceType.values().length])
					.location("Location" + (i %3) + 1)
					.userId((long) ((i % numberOfUsers) + 1))
					.build();
			deviceRepository.save(device);
		}
		log.info("Devices created");
	}
}
