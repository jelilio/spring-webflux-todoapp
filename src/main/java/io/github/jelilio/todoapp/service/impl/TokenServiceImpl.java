package io.github.jelilio.todoapp.service.impl;

import io.github.jelilio.todoapp.config.properties.AuthProperties;
import io.github.jelilio.todoapp.dto.AuthRequestDto;
import io.github.jelilio.todoapp.dto.RefreshTokenDto;
import io.github.jelilio.todoapp.entity.RefreshTokenCache;
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
import org.springframework.security.core.userdetails.UserDetails;
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

  private String generateToken(String subject, Collection<? extends GrantedAuthority> authorities) {
    Instant now = Instant.now();
    String scope = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));
    JwtClaimsSet claims = JwtClaimsSet.builder()
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
  public Mono<AuthResponse> generateToken(AuthRequestDto dto) {
    LOG.debug("generateToken: {}", dto.username());
    var authMono = authManager
        .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password()));

    return authMono.flatMap(authentication -> saveRefreshToken(authentication.getName())
        .map(this::generateRefreshToken)
        .map(rfTkn -> new AuthResponse(generateToken(
            authentication.getName(), authentication.getAuthorities()), rfTkn)));
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
    return userService.findByUsername(rfTkn.getUsername())
        .flatMap(userDetails -> {
          var rfToken =  jwt.getTokenValue();
          var acToken = generateToken(userDetails.getUsername(), userDetails.getAuthorities());

          return Mono.just(new AuthResponse(acToken, rfToken));
        });
  }

  private Mono<String> saveRefreshToken(String username) {
    return reactiveMongoTemplate.insert(new RefreshTokenCache(username, Duration.ofDays(authProperties.expiration().refresh())))
        .flatMap(it -> reactiveMongoTemplate.indexOps(RefreshTokenCache.class)
            .ensureIndex(new Index().on("expiredDate", Sort.Direction.ASC).expire(0))
            .map(__ -> it.getId()));
  }

  @Override
  public Mono<AuthResponse> generateToken(UserDetails user) {
    return saveRefreshToken(user.getUsername())
        .map(this::generateRefreshToken)
        .map(rfTkn -> new AuthResponse(generateToken(user.getUsername(),
            user.getAuthorities()), rfTkn));
  }
}