package io.github.jelilio.todoapp.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(String issuer, ExpirationProperties expiration, OtpProperties otp) {
  public record ExpirationProperties(Integer access, Integer refresh) { }
  public record OtpProperties(Integer duration) { }
}
