package io.github.jelilio.todoapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BasicRegisterDto (
    @NotNull @NotBlank @NotEmpty
    String name,
    @NotNull @NotBlank @NotEmpty @Email
    String email,
    @NotNull @NotBlank @NotEmpty
    String password
) {
}
