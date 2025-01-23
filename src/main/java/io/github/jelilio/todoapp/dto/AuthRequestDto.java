package io.github.jelilio.todoapp.dto;

public record AuthRequestDto(
    String username,
    String password
) {
}
