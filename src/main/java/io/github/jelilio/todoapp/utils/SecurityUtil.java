package io.github.jelilio.todoapp.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import reactor.core.publisher.Mono;

public abstract class SecurityUtil {
  public static Mono<String> loggedInUsername() {
    return loggedInCredentials()
        .map(JwtClaimAccessor::getSubject);
  }

  public static Mono<Jwt> loggedInCredentials() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> context.getAuthentication().getPrincipal())
        .cast(Jwt.class);
  }

  public static Mono<String> loggedInUserId() {
    return loggedInCredentials()
        .map(JwtClaimAccessor::getId);
  }
}
