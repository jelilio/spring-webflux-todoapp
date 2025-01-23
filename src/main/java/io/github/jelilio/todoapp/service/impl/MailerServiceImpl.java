package io.github.jelilio.todoapp.service.impl;

import io.github.jelilio.todoapp.entity.User;
import io.github.jelilio.todoapp.service.MailerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MailerServiceImpl implements MailerService {
  private static final Logger LOG = LoggerFactory.getLogger(MailerServiceImpl.class);

  @Override
  public Mono<String> sendOtpMail(User user, String otpKey, Integer otpKeyDuration) {
    LOG.debug("sendOtpMail: mail: {}, key: {}", user.getEmail(), otpKey);
    return Mono.just("mail sent");
  }

  @Override
  public Mono<String> sendActivationMail(User user) {
    LOG.debug("sendActivationMail: mail: {}", user.getEmail());
    return Mono.just("mail sent");
  }
}
