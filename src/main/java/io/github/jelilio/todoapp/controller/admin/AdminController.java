package io.github.jelilio.todoapp.controller.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearer_auth")
public class AdminController {
  @GetMapping("/")
  public Mono<Principal> home(Principal principal) {
    return Mono.just(principal);
  }
}
