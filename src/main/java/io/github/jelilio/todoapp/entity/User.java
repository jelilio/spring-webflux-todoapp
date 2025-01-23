package io.github.jelilio.todoapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.jelilio.todoapp.entity.base.AbstractAuditingEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jelilio.todoapp.utils.Constants.ROLE_USER;

@Document(collection = "users")
public class User extends AbstractAuditingEntity implements UserDetails {
  @Id
  private String id;
  private String name;
  private String email;
  private String username;
  private Instant lastLoginDate;

  @JsonIgnore
  private String password;
  @JsonIgnore
  private Instant enabledDate;
  @JsonIgnore
  private Instant activatedDate;
  @JsonIgnore
  private Set<String> roles;

  public User() {
    this.enabledDate = Instant.now();
    this.roles = Set.of(ROLE_USER);
  }

  public User(String email) {
    this();
    this.email = email;
    this.username = email;
  }

  public User(String email, String password) {
    this(email);
    this.password = password;
  }

  public User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.username = email;
    this.password = password;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @JsonIgnore
  public boolean isEnabled() {
    return enabledDate != null;
  }

  @JsonIgnore
  public boolean isDisabled() {
    return enabledDate == null;
  }

  @JsonIgnore
  public boolean isActivated() {
    return activatedDate != null;
  }

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream()
        .map(authority -> new SimpleGrantedAuthority(authority.toLowerCase()))
        .collect(Collectors.toList());
  }

  @Override
  @JsonIgnore
  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  @JsonIgnore
  public String getUsername() {
    return this.username;
  }

  public void setActivatedDate(Instant activatedDate) {
    this.activatedDate = activatedDate;
  }

  public void setLastLoginDate(Instant lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
  }

  @Override
  public String toString() {
    return "User{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
