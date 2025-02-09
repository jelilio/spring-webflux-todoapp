package io.github.jelilio.todoapp.service;

import io.github.jelilio.todoapp.config.properties.AuthProperties;
import io.github.jelilio.todoapp.repository.UserRepository;
import io.github.jelilio.todoapp.service.impl.UserServiceImpl;
import io.github.jelilio.todoapp.utils.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

@SpringBootTest
public class UserServiceTest {
  @MockitoBean
  ReactiveJwtDecoder jwtDecoder;

  @Autowired
  RandomUtil randomUtil;

  @Autowired
  AuthProperties authProperties;

  @MockitoBean
  UserRepository userRepository;

  @MockitoBean
  PasswordEncoder passwordEncoder;

  @Autowired
  OtpService otpService;

  @Autowired
  MailerService mailerService;

  UserService userService;

  private static final String USER_ID = "67917453ef9f020e59b4c5a8";

  @BeforeEach
  public void setUp() {
    userService = new UserServiceImpl(randomUtil, authProperties, userRepository,
        passwordEncoder, otpService, mailerService);


    Mockito.when(userRepository.findAll()).thenReturn(Flux.empty());
    Mockito.when(userRepository.countById(USER_ID)).thenReturn(Mono.just(1L));
  }

  @Test
  public void canFindAll() {
    StepVerifier.create(userService.findAll())
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @WithMockUser(username = USER_ID)
  public void canGetLoggedInUserId() {
    StepVerifier.create(userService.getLoggedInUserId())
        .expectNext(USER_ID)
        .verifyComplete();
  }

  private Jwt jwt() {
    return new Jwt("token", null, null,
        Map.of("alg", "none"), Map.of("sub", "betsy", "jti", USER_ID)
    );
  }
}
