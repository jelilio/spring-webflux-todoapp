package io.github.jelilio.todoapp.service;

import io.github.jelilio.todoapp.dto.BasicRegisterDto;
import io.github.jelilio.todoapp.entity.User;
import org.springframework.data.util.Pair;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService extends ReactiveUserDetailsService {
  Mono<User> findByUserId(String userId);

  Mono<Boolean> checkIfEmailAvailable(String email);

  Mono<Pair<User, Integer>> register(BasicRegisterDto dto);

  Mono<User> verifyEmail(String email, String otpKey);

  Mono<Pair<User, Integer>> requestOtp(String usernameOrEmail);

  Flux<User> findAll();
}
