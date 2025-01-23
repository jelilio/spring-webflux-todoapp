package io.github.jelilio.todoapp.controller;

import io.github.jelilio.todoapp.dto.BasicRegisterDto;
import io.github.jelilio.todoapp.dto.ValidateOtpDto;
import io.github.jelilio.todoapp.model.AuthResponse;
import io.github.jelilio.todoapp.service.TokenService;
import io.github.jelilio.todoapp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {
  private final UserService userService;
  private final TokenService tokenService;

  public AccountController(UserService userService, TokenService tokenService) {
    this.userService = userService;
    this.tokenService = tokenService;
  }

  @PostMapping("/register")
  public Mono<ResponseEntity<Map<String, Integer>>> register(@Valid @RequestBody BasicRegisterDto ar) {
    return userService.register(ar)
        .map(inserted -> ResponseEntity
            .created(URI.create("/api/account/register/" + inserted.getFirst().getId()))
            .body(Map.of("duration", inserted.getSecond())));
  }

  @PostMapping("/verify-email-otp")
  public Mono<ResponseEntity<AuthResponse>> verifyEmail(@Valid @RequestBody ValidateOtpDto dto) {
    return userService.verifyEmail(dto.email(), dto.otpKey())
        .flatMap(tokenService::generateToken)
        .map(ResponseEntity::ok);
  }

  @GetMapping("/check-email")
  public Mono<ResponseEntity<Boolean>> checkEmail(@NotNull @RequestParam("email") String email) {
    return userService.checkIfEmailAvailable(email)
        .map(ResponseEntity::ok);
  }

  @PostMapping("/request-otp")
  public Mono<ResponseEntity<Map<String, Integer>>> requestOtp(@Valid @RequestBody String email) {
    return userService.requestOtp(email)
        .map(userLongTuple -> ResponseEntity
            .ok(Map.of("duration", userLongTuple.getSecond()))
        );
  }
}