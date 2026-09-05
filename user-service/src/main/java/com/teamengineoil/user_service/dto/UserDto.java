package com.teamengineoil.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private long id;

    @NotBlank(message = "{validation.firstname.required}")
    private String firstname;

    @NotBlank(message = "{validation.lastname.required}")
    private String lastname;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    private String address;

    private boolean alerting;

    private double energyAlertingThreshold;
}