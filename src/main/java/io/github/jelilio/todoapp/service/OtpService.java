package io.github.jelilio.todoapp.service;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

public interface OtpService {
  Mono<String> createOtp(String email, String otp, Duration duration);

  Mono<Optional<String>> getOtp(String email);
}
