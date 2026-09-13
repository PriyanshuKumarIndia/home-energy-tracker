package com.teamengineoil.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.teamengineoil.kafka.event.AlertingEvent;
import com.teamengineoil.kafka.event.EnergyUsageEvent;
import com.teamengineoil.usage_service.client.DeviceClient;
import com.teamengineoil.usage_service.client.UserClient;
import com.teamengineoil.usage_service.dto.DeviceDto;
import com.teamengineoil.usage_service.dto.UsageDto;
import com.teamengineoil.usage_service.dto.UserDto;
import com.teamengineoil.usage_service.model.Device;
import com.teamengineoil.usage_service.model.DeviceEnergy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsageService {
    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsage(EnergyUsageEvent energyUsageEvent) {
        log.info("Received energy usage event: {}", energyUsageEvent);
        Point point = Point.measurement("energy-usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        influxDBClient
                .getWriteApiBlocking()
                .writePoint(influxBucket, influxOrg, point);

    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        List<FluxTable> tables = getFluxTables(now, now.minus(Duration.ofHours(1)), null);
        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table : tables) {
            for (FluxRecord fluxRecord : table.getRecords()) {
                String deviceIdStr = (String) fluxRecord.getValueByKey("deviceId");
                Object value = fluxRecord.getValueByKey("_value");
                double energyConsumed = value instanceof Number ? ((Number) value).doubleValue() : 0.0;

                if (deviceIdStr == null) {
                    log.warn("deviceId is missing from Flux record: {}", fluxRecord);
                    continue;
                }

                deviceEnergies.add(
                        DeviceEnergy.builder()
                                .deviceId(Long.valueOf(deviceIdStr))
                                .energyConsumed(energyConsumed)
                                .build()
                );
            }
        }
        log.info("Aggregate device energy usage event over the past hour: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            final DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());

            if (deviceResponse == null || deviceResponse.id() == null) {
                log.warn("Device with id {} not found", deviceEnergy.getDeviceId());
                continue;
            }

            deviceEnergy.setUserId(deviceResponse.userId());

        }
        deviceEnergies.removeIf(de -> de.getUserId() == null);
        Map<Long, List<DeviceEnergy>> userDeviceMap =
                deviceEnergies.stream().collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device map: {}", userDeviceMap);

        List<Long> userIds = new ArrayList<>(userDeviceMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for (final Long userId : userIds) {
            try {
                UserDto user = userClient.getUserById(userId);
                if (user == null || !user.alerting()) {
                    log.warn("User with id {} not found or alerting is disabled for this user", userId);
                    continue;
                }
                userThresholdMap.put(user.id(), user.energyAlertingThreshold());
                userEmailMap.put(user.id(), user.email());
            } catch (Exception ex) {
                log.error("Error occurred while getting user threshold for user with id {}", userId, ex);
            }
        }
        log.info("User-Threshold Map: {}", userThresholdMap);

        final List<Long> alertEnabledUsers = new ArrayList<>(userThresholdMap.keySet());
        for (final Long userId : alertEnabledUsers) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> deviceEnergyList = userDeviceMap.get(userId);

            final double totalConsumptions = deviceEnergyList.stream().mapToDouble(DeviceEnergy::getEnergyConsumed).sum();

            if (totalConsumptions > threshold) {
                log.info("Alerting threshold hit for user with id {} and the consumption is {}", userId, totalConsumptions);

                final AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("Energy consumption threshold exceeded")
                        .energyConsumed(totalConsumptions)
                        .threshold(threshold)
                        .email(userEmailMap.get(userId))
                        .build();

                kafkaTemplate.send("energy-usage-alerting", alertingEvent);
            } else {
                log.info("User Id {} is safe and consumption({}) under limit", userId, totalConsumptions);
            }
        }
    }

    private @NonNull List<FluxTable> getFluxTables(Instant end, Instant start, String filter) {
        String fluxQuery = "";

        if (filter != null) {
            fluxQuery = String.format("""
                            from(bucket: "%s")
                              |> range(start: time(v: "%s"), stop: time(v: "%s"))
                              |> filter(fn: (r) => r["_measurement"] == "energy-usage")
                              |> filter(fn: (r) => r["_field"] == "energyConsumed")
                              |> filter(fn: (r) => %s)
                              |> group(columns: ["deviceId"])
                              |> sum(column: "_value")
                            """,
                    influxBucket,
                    start,
                    end,
                    filter
            );
        } else {
            fluxQuery = String.format("""
                            from(bucket: "%s")
                              |> range(start: time(v: "%s"), stop: time(v: "%s"))
                              |> filter(fn: (r) => r["_measurement"] == "energy-usage")
                              |> filter(fn: (r) => r["_field"] == "energyConsumed")
                              |> group(columns: ["deviceId"])
                              |> sum(column: "_value")
                            """,
                    influxBucket,
                    start,
                    end);
        }

        log.info("Executing Flux query: {}", fluxQuery);
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery, influxOrg);
    }

    public UsageDto getXDaysUsageForUser(Long userId, int days) {
        log.info("Getting {} days usage for user with id {}", days, userId);
        List<DeviceDto> devicesDto = deviceClient.getAllDevicesForUser(userId);

        if (devicesDto == null || devicesDto.isEmpty()) {
            return UsageDto.builder()
                    .userId(userId)
                    .devices(devicesDto)
                    .build();
        }
        final List<Device> devices = new ArrayList<>();
        for (DeviceDto deviceDto : devicesDto) {
            devices.add(Device.builder()
                    .id(deviceDto.id())
                    .name(deviceDto.name())
                    .type(deviceDto.type())
                    .location(deviceDto.location())
                    .userId(deviceDto.userId())
                    .build());
        }

        List<String> deviceIdStrings = devicesDto.stream()
                .map(DeviceDto::id)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
        final Instant now = Instant.now();
        final String deviceFilter = deviceIdStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));
        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            List<FluxTable> tables = getFluxTables(now, now.minusSeconds((long) days * 24 * 3600), deviceFilter);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj == null ? null : deviceIdObj.toString();
                    if (deviceIdStr == null) continue;

                    Double energyConsumed = record.getValueByKey("_value") instanceof Number
                            ? ((Number) record.getValueByKey("_value")).doubleValue()
                            : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(deviceId, aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException nfe) {
                        log.warn("Failed to parse deviceId from flux record: {}", deviceIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query InfluxDB for user {} usage over {} days: {}", userId, days, e.getMessage());
            // set aggregatedConsumption to 0.0 on error
            devices.forEach(d -> d.setEnergyConsumed(0.0));
            return UsageDto.builder()
                    .userId(userId)
                    .devices(null)
                    .build();
        }
        // populate aggregated energy consumed per device
        for (Device device : devices) {
            if (device == null || device.getId() == null) continue;
            device.setEnergyConsumed(aggregatedMap.getOrDefault(device.getId(), 0.0));
        }

        log.info("Aggregated energy consumption for userId {}: {}", userId, aggregatedMap);

        final List<DeviceDto> resultDevices = devices.stream()
                .map(d -> DeviceDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .type(d.getType())
                        .location(d.getLocation())
                        .userId(d.getUserId())
                        .energyConsumed(d.getEnergyConsumed())
                        .build())
                .toList();

        return UsageDto.builder()
                .userId(userId)
                .devices(resultDevices)
                .build();

    }
}
