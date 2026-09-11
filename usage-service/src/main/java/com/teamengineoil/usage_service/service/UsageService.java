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
import com.teamengineoil.usage_service.dto.UserDto;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<FluxTable> tables = getFluxTables(now);
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

    private @NonNull List<FluxTable> getFluxTables(Instant now) {
        final Instant oneHourAgo = now.minus(Duration.ofHours(1));

        String fluxQuery = String.format("""
                        from(bucket: "%s")
                          |> range(start: time(v: "%s"), stop: time(v: "%s"))
                          |> filter(fn: (r) => r["_measurement"] == "energy-usage")
                          |> filter(fn: (r) => r["_field"] == "energyConsumed")
                          |> group(columns: ["deviceId"])
                          |> sum(column: "_value")
                        """,
                influxBucket,
                oneHourAgo,
                now
        );

        log.info("Executing Flux query: {}", fluxQuery);
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(fluxQuery, influxOrg);
    }
}
