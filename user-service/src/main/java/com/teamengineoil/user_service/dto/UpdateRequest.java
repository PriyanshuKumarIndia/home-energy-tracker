package com.teamengineoil.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {

    private String firstname;

    private String lastname;

    @Email(message = "{validation.email.invalid}")
    private String email;

    private String address;

    private boolean alerting;

    private double energyAlertingThreshold;
}