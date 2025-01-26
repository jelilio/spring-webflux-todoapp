package io.github.jelilio.todoapp.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public abstract class SecurityUtil {
  public static Mono<String> loggedInUsername() {
    return ReactiveSecurityContextHolder.getContext()
        .mapNotNull(context -> {
          var auth = context.getAuthentication();
          if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
          } else if (auth.getPrincipal() instanceof String principal) {
            return principal;
          } else if (auth.getPrincipal() instanceof User user) {
            return user.getUsername();
          }
          return null;
        });
  }

  public static Mono<String> loggedInUserId() {
    return ReactiveSecurityContextHolder.getContext()
        .mapNotNull(context -> {
          var auth = context.getAuthentication();
          if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getId();
          } else if (auth.getPrincipal() instanceof String principal) {
            return principal;
          } else if (auth.getPrincipal() instanceof User user) {
            return user.getUsername();
          }
          return null;
        });
  }
}
