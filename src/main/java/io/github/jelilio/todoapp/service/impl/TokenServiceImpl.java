package io.github.jelilio.todoapp.service.impl;

import io.github.jelilio.todoapp.config.properties.AuthProperties;
import io.github.jelilio.todoapp.dto.AuthRequestDto;
import io.github.jelilio.todoapp.dto.RefreshTokenDto;
import io.github.jelilio.todoapp.entity.RefreshTokenCache;
import io.github.jelilio.todoapp.entity.User;
import io.github.jelilio.todoapp.exception.AuthenticationException;
import io.github.jelilio.todoapp.model.AuthResponse;
import io.github.jelilio.todoapp.service.TokenService;
import io.github.jelilio.todoapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements TokenService {
  private static final Logger LOG = LoggerFactory.getLogger(TokenServiceImpl.class);

  private final JwtEncoder encoder;
  private final JwtEncoder refreshTokenEncoder;
  private final AuthProperties authProperties;
  private final UserService userService;
  private final ReactiveAuthenticationManager authManager;
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private final ReactiveAuthenticationManager refreshTokenAuthManager;

  public TokenServiceImpl(
      JwtEncoder jwtEncoder,
      ReactiveAuthenticationManager authManager,
      UserService userService,
      AuthProperties authProperties,
      ReactiveMongoTemplate reactiveMongoTemplate,
      @Qualifier("jwtRefreshTokenEncoder") JwtEncoder refreshTokenEncoder,
      @Qualifier("jwtRefreshTokenAuthenticationManager") ReactiveAuthenticationManager refreshTokenAuthManager) {
    this.encoder = jwtEncoder;
    this.userService = userService;
    this.authProperties = authProperties;
    this.refreshTokenEncoder = refreshTokenEncoder;
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.refreshTokenAuthManager = refreshTokenAuthManager;
    this.authManager = authManager;
  }

  private String generateToken(String id, String subject, Collection<? extends GrantedAuthority> authorities) {
    Instant now = Instant.now();
    String scope = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));
    LOG.debug("token to expired in: {} seconds", authProperties.expiration().access());
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .id(id)
        .issuer(authProperties.issuer())
        .issuedAt(now)
        .expiresAt(now.plus(authProperties.expiration().access(), ChronoUnit.SECONDS))
        .subject(subject)
        .claim("scope", scope)
        .build();
    return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private String generateRefreshToken(String subject) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(authProperties.issuer())
        .issuedAt(now)
        .expiresAt(now.plus(authProperties.expiration().refresh(), ChronoUnit.DAYS))
        .subject(subject)
        .claim("scope", "")
        .build();
    return this.refreshTokenEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  @Override
  public Mono<AuthResponse> generateToken(Authentication authentication) {
    return saveRefreshToken(authentication.getName())
        .map(this::generateRefreshToken)
        .map(rfTkn -> {
          var user = (User) authentication.getPrincipal();
          var userInfo = new AuthResponse.UserInfo(user.getName(), user.getEmail(),
              user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
          return new AuthResponse(generateToken(
              user.getId(), authentication.getName(), authentication.getAuthorities()),
              rfTkn, userInfo
          );
        });
  }

  @Override
  public Mono<AuthResponse> generateToken(AuthRequestDto dto) {
    var authMono = authManager
        .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password()));

    return authMono.flatMap(this::generateToken);
  }

  @Override
  public Mono<AuthResponse> refreshToken(RefreshTokenDto tokenDto) {
    Mono<Authentication> authentication = refreshTokenAuthManager
        .authenticate(new BearerTokenAuthenticationToken(tokenDto.token()));

    return authentication.flatMap(it -> {
      var jwt = (Jwt) it.getCredentials();

      return reactiveMongoTemplate.findById(jwt.getSubject(), RefreshTokenCache.class)
          .flatMap(rfTkn -> generateToken(rfTkn, jwt))
          .switchIfEmpty(Mono.error(() -> new AuthenticationException("Invalid refresh token")));
    });
  }

  public Mono<AuthResponse> generateToken(RefreshTokenCache rfTkn, Jwt jwt) {
    return userService.findByUsernameOrEmail(rfTkn.getUsername())
        .flatMap(user -> {
          var rfToken =  jwt.getTokenValue();
          var acToken = generateToken(user.getId(), user.getUsername(), user.getAuthorities());
          var userInfo = new AuthResponse.UserInfo(user.getName(), user.getEmail(),
              user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));

          return Mono.just(new AuthResponse(acToken, rfToken, userInfo));
        });
  }

  private Mono<String> saveRefreshToken(String username) {
    return reactiveMongoTemplate.insert(new RefreshTokenCache(username, Duration.ofDays(authProperties.expiration().refresh())))
        .flatMap(it -> reactiveMongoTemplate.indexOps(RefreshTokenCache.class)
            .ensureIndex(new Index().on("expiredDate", Sort.Direction.ASC).expire(0))
            .map(__ -> it.getId()));
  }

  @Override
  public Mono<AuthResponse> generateToken(User user) {
    var userInfo = new AuthResponse.UserInfo(user.getName(), user.getEmail(),
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));

    return saveRefreshToken(user.getUsername())
        .map(this::generateRefreshToken)
        .map(rfTkn -> new AuthResponse(generateToken(user.getId(), user.getUsername(),
            user.getAuthorities()), rfTkn, userInfo));
  }
}