package io.github.jelilio.todoapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/")
public class IndexController {
  @GetMapping("/")
  public Mono<Principal> home(Principal principal) {
    return Mono.just(principal);
  }
}
