package io.github.jelilio.todoapp.model;

import java.util.Set;

public record AuthResponse(String accessToken, String refreshToken, UserInfo userInfo) {
  public record UserInfo(String name, String email, Set<String> roles) { }
}
