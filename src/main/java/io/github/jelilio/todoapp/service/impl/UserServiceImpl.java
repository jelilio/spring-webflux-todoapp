package io.github.jelilio.todoapp.service.impl;

import io.github.jelilio.todoapp.config.properties.AuthProperties;
import io.github.jelilio.todoapp.dto.BasicRegisterDto;
import io.github.jelilio.todoapp.entity.User;
import io.github.jelilio.todoapp.exception.AlreadyExistException;
import io.github.jelilio.todoapp.exception.AuthenticationException;
import io.github.jelilio.todoapp.repository.UserRepository;
import io.github.jelilio.todoapp.service.MailerService;
import io.github.jelilio.todoapp.service.OtpService;
import io.github.jelilio.todoapp.service.UserService;
import io.github.jelilio.todoapp.utils.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static io.github.jelilio.todoapp.exception.AuthenticationException.*;
import static io.github.jelilio.todoapp.utils.Constants.ZERO;

@Service
public class UserServiceImpl implements UserService, ReactiveUserDetailsService {
  private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

  private final RandomUtil randomUtil;
  private final AuthProperties authProperties;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final OtpService otpService;
  private final MailerService mailerService;

  public UserServiceImpl(
      RandomUtil randomUtil, AuthProperties authProperties, UserRepository userRepository,
      PasswordEncoder passwordEncoder, OtpService otpService, MailerService mailerService) {
    this.randomUtil = randomUtil;
    this.authProperties = authProperties;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.otpService = otpService;
    this.mailerService = mailerService;
  }

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    LOG.debug("findByUsername: {}", username);
    return userRepository.findByUsernameOrEmailAndActivated(username);
  }

  @Override
  public Mono<User> findByUserId(String userId) {
    LOG.debug("findByUserId: {}", userId);
    return userRepository.findById(userId);
  }

  @Override
  public Mono<Boolean> checkIfEmailAvailable(String email) {
    LOG.debug("checkIfEmailAvailable: {}", email);
    return userRepository.countByEmailAvailable(email)
        .map(count -> count == ZERO);
  }

  @Transactional
  @Override
  public Mono<Pair<User, Integer>> register(BasicRegisterDto dto) {
    Mono<Boolean> uniEmailAvailable = checkIfEmailAvailable(dto.email());
    Mono<User> extLoginUni = userRepository.findByEmailAndNotActivated(dto.email())
        .defaultIfEmpty(new User());

    return Mono.zip(uniEmailAvailable, extLoginUni).flatMap(tuple2 -> {
      var emailAvailable = tuple2.getT1();

      if(!emailAvailable) {
        return Mono.error(() -> new AlreadyExistException("A user with this email already exist"));
      }

      final User user = tuple2.getT2();
      user.setName(dto.name());
      user.setEmail(dto.email());
      user.setUsername(dto.email());
      user.setPassword(passwordEncoder.encode(dto.password()));

      return userRepository.save(user).flatMap(updated -> createOtp(dto.email(), updated));
    });
  }

  @Override
  public Mono<User> verifyEmail(String email, String otpKey) {
    return validateOtp(email, otpKey).flatMap(user -> {
      user.setActivatedDate(Instant.now());
      user.setLastLoginDate(Instant.now());
      return userRepository.save(user);
    }).flatMap(user -> mailerService.sendActivationMail(user)
        .map(__ -> user)
    );
  }

  @Override
  @Transactional
  public Mono<Pair<User, Integer>> requestOtp(String usernameOrEmail) {
    Mono<User> loginUni = userRepository.findUserByUsernameOrEmail(usernameOrEmail)
        .switchIfEmpty(Mono.error(() -> new AuthenticationException("Email or username not registered", AUTH_LOGIN_INVALID)));

    return loginUni.flatMap(user -> {
      if (user.isDisabled()) {
        return Mono.error(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(user.isActivated()) {
        return Mono.error(() -> new AuthenticationException("Already activated", AUTH_LOGIN_ACTIVATED));
      }

      return createOtp(user.getEmail(), user);
    });
  }

  @Override
  public Flux<User> findAll() {
    return userRepository.findAll();
  }

  @Transactional
  public Mono<Pair<User, Integer>> createOtp(String email, User user) {
    var otpKey = randomUtil.generateOtp();

    return otpService.createOtp(email, otpKey, Duration.ofSeconds(authProperties.otp().duration()))
        .flatMap(__ -> mailerService.sendOtpMail(user, otpKey, authProperties.otp().duration())
            .map(it -> Pair.of(user, authProperties.otp().duration())));
  }

  private Mono<User> validateOtp(String usernameOrEmail, String otpKey) {
    Mono<User> userUni = userRepository.findUserByUsernameOrEmail(usernameOrEmail)
        .switchIfEmpty(Mono.error(() -> new AuthenticationException("No user with this email or username found", AUTH_LOGIN_INVALID)));

    return userUni.flatMap(user -> {
      if(user.isDisabled()) {
        return Mono.error(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(user.isActivated()) {
        return Mono.error(() -> new AuthenticationException("Already activated", AUTH_LOGIN_ACTIVATED));
      }

      return otpService.getOtp(usernameOrEmail).flatMap(response -> {
        if(response.isEmpty()) {
          return Mono.error(() -> new AuthenticationException("Expired OTP", AUTH_OTP_EXPIRED));
        }

        if(!response.get().equalsIgnoreCase(otpKey)) {
          return Mono.error(() -> new AuthenticationException("Invalid OTP", AUTH_OTP_INVALID));
        }

        return Mono.just(user);
      });
    });
  }
}
