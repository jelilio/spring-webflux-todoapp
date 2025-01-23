package io.github.jelilio.todoapp.controller;

import io.github.jelilio.todoapp.dto.AuthRequestDto;
import io.github.jelilio.todoapp.dto.RefreshTokenDto;
import io.github.jelilio.todoapp.model.AuthResponse;
import io.github.jelilio.todoapp.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

  private final TokenService tokenService;

  public AuthController(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @PostMapping("/token")
  public Mono<AuthResponse> token(@RequestBody AuthRequestDto authentication) {
    return tokenService.generateToken(authentication);
  }

  @PostMapping("/refresh")
  public Mono<AuthResponse> refresh(@RequestBody RefreshTokenDto tokenDto) {
    return tokenService.refreshToken(tokenDto);
  }
}
