package io.github.jelilio.todoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateOtpDto(
    @NotNull @NotBlank
    String email,
    @NotNull @NotBlank
    String otpKey
) {
}
