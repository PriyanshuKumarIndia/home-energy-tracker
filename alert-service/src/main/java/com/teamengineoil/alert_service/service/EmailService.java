package com.teamengineoil.alert_service.service;

import com.teamengineoil.alert_service.entity.Alert;
import com.teamengineoil.alert_service.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;

    public void sendEmail(String to, String subject, String body, Long userId) {
        log.info("Sending email to {} userId: {} with subject {}", to, userId, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@teamengineoil.com");

        try {
            mailSender.send(message);
            final Alert alert = Alert.builder()
                    .sent(true)
                    .createdAt(LocalDateTime.now())
                    .userId(userId)
                    .build();
            alertRepository.save(alert);
        } catch (Exception e) {
            log.error("Send email failed!", e);
            final Alert alert = Alert.builder()
                    .sent(false)
                    .createdAt(LocalDateTime.now())
                    .userId(userId)
                    .build();
            alertRepository.saveAndFlush(alert);
        }
    }
}
