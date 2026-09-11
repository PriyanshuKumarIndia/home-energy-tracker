package com.teamengineoil.alert_service.service;

import com.teamengineoil.kafka.event.AlertingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {
    private final EmailService emailService;

    @KafkaListener(topics = "energy-usage-alerting", groupId = "alert-service")
    public void energyUsageAlerting(AlertingEvent alertingEvent) {
        log.info("Received alerting event: {}", alertingEvent);
        final String subject = "Energy Usage Alerting for userId: " + alertingEvent.getUserId();
        final String body =
                "Alert: " + alertingEvent.getMessage() + "\nThreshold: " + alertingEvent.getThreshold() + "\nEnergy " +
                        "Consumed: " + alertingEvent.getEnergyConsumed();

        emailService.sendEmail(alertingEvent.getEmail(), subject, body, alertingEvent.getUserId());
    }
}
