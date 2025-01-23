package io.github.jelilio.todoapp.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

public abstract class AbstractAuditingEntity {
  @JsonIgnore
  @CreatedBy
  private String createdBy;

  @JsonIgnore
  @CreatedDate
  private Instant createdDate;

  @JsonIgnore
  @LastModifiedBy
  private String lastModifiedBy;

  @JsonIgnore
  @LastModifiedDate
  private Instant lastModifiedDate;
}
