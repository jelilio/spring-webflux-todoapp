package io.github.jelilio.todoapp.exception.model;

import java.time.Instant;

public record ErrorDetail(
    int status,
    String error,
    String message,
    Instant timestamp
) {
}
