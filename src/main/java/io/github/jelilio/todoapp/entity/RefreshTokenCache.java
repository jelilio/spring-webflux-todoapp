package io.github.jelilio.todoapp.entity;

import io.github.jelilio.todoapp.entity.base.AbstractAuditingEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;

@Document(collection = "refreshTokens")
public class RefreshTokenCache extends AbstractAuditingEntity {
  @Id
  private String id;
  private String username;
  private Instant expiredDate;

  public RefreshTokenCache() {}

  public RefreshTokenCache(String username, Duration duration) {
    this.username = username;
    this.expiredDate = Instant.now().plus(duration);
  }

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }
}
