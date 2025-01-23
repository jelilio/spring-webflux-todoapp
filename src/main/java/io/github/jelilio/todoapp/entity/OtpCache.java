package io.github.jelilio.todoapp.entity;

import io.github.jelilio.todoapp.entity.base.AbstractAuditingEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;

@Document(collection = "otpCaches")
public class OtpCache extends AbstractAuditingEntity {
  @Id
  private String id;
  private String otpKey;
  private Instant expiredDate;

  @Version
  private Integer version;

  public OtpCache() {}

  public OtpCache(String email, String otpKey, Duration duration) {
    this.id = email;
    this.otpKey = otpKey;
    this.expiredDate = Instant.now().plus(duration);
  }

  public String getEmail() {
    return id;
  }

  public String getOtpKey() {
    return otpKey;
  }
}
