package io.github.jelilio.todoapp.service;

import io.github.jelilio.todoapp.entity.User;
import reactor.core.publisher.Mono;

public interface MailerService {
  Mono<String> sendOtpMail(User user, String otpKey, Integer otpKeyDuration);

  Mono<String> sendActivationMail(User user);
}
