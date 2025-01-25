package io.github.jelilio.todoapp.service;

import io.github.jelilio.todoapp.dto.AuthRequestDto;
import io.github.jelilio.todoapp.dto.RefreshTokenDto;
import io.github.jelilio.todoapp.entity.User;
import io.github.jelilio.todoapp.model.AuthResponse;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface TokenService {
  Mono<AuthResponse> generateToken(AuthRequestDto dto);

  Mono<AuthResponse> refreshToken(RefreshTokenDto dto);

  Mono<AuthResponse> generateToken(User user);
}
