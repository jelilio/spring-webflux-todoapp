package io.github.jelilio.todoapp.config;

import io.github.jelilio.todoapp.utils.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;

import static io.github.jelilio.todoapp.utils.Constants.SYSTEM;

@Configuration
public class DatabaseConfig {
  @Bean
  public ReactiveAuditorAware<String> auditorAware() {
    return () -> SecurityUtil.loggedInUsername().defaultIfEmpty(SYSTEM);
  }
}
