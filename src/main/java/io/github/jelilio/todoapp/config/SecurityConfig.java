package io.github.jelilio.todoapp.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.jelilio.todoapp.config.properties.CorsProperties;
import io.github.jelilio.todoapp.config.properties.RsaKeyProperties;
import io.github.jelilio.todoapp.entity.User;
import io.github.jelilio.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
  private final CorsProperties corsProperties;
  private final RsaKeyProperties rsaKeyProperties;

  public SecurityConfig(CorsProperties corsProperties, RsaKeyProperties rsaKeyProperties) {
    this.corsProperties = corsProperties;
    this.rsaKeyProperties = rsaKeyProperties;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(it -> it.configurationSource(corsConfigurationSource()))
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/api/admin/**").hasRole("ROLE_ADMIN")
            .pathMatchers(HttpMethod.POST,"/api/auth/refresh").permitAll()
            .pathMatchers(HttpMethod.POST,"/api/auth/token").permitAll()
            .pathMatchers(HttpMethod.POST, "/api/account/**").permitAll()
            .pathMatchers("/api/account/check-email").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/webjars/**", "/swagger-ui.html").permitAll()
            .pathMatchers(HttpMethod.GET,"/.well-known/**").permitAll()
            .anyExchange().authenticated()
        )
        .exceptionHandling(ex -> {
          ex.authenticationEntryPoint(new BearerTokenServerAuthenticationEntryPoint());
          ex.accessDeniedHandler(new BearerTokenServerAccessDeniedHandler());
        })
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .oauth2ResourceServer((oauth2Spec) ->
            oauth2Spec.jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

    return http.build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    var jwtConverter = new ReactiveJwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(this::jwtRoleConverter);
    return jwtConverter;
  }

  private Flux<GrantedAuthority> jwtRoleConverter(Jwt jwt) {
    String scope = jwt.getClaimAsString("scope");
    String[] roles = scope.split("//s+");
    Set<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
        .map(it -> new SimpleGrantedAuthority(it.toUpperCase())).collect(Collectors.toSet());
    return Flux.fromIterable(authorities);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  @Primary
  public ReactiveJwtDecoder jwtDecoder() {
    return NimbusReactiveJwtDecoder.withPublicKey(rsaKeyProperties.access().publicKey()).build();
  }

  @Bean
  @Primary
  public JwtEncoder jwtEncoder() {
    JWK jwk = new RSAKey
        .Builder(rsaKeyProperties.access().publicKey())
        .privateKey(rsaKeyProperties.access().privateKey())
        .build();
    JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }

  @Bean
  @Qualifier("jwtRefreshTokenDecoder")
  public ReactiveJwtDecoder jwtRefreshTokenDecoder() {
    return NimbusReactiveJwtDecoder.withPublicKey(rsaKeyProperties.refresh().publicKey()).build();
  }

  @Bean
  @Qualifier("jwtRefreshTokenEncoder")
  public JwtEncoder jwtRefreshTokenEncoder() {
    JWK jwk = new RSAKey
        .Builder(rsaKeyProperties.refresh().publicKey())
        .privateKey(rsaKeyProperties.refresh().privateKey())
        .build();
    JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }

  @Bean
  @Qualifier("jwtRefreshTokenAuthenticationManager")
  public ReactiveAuthenticationManager jwtRefreshTokenAuthenticationManager() {
    var provider = new JwtReactiveAuthenticationManager(jwtRefreshTokenDecoder());
    provider.setJwtAuthenticationConverter((Jwt source) -> Mono.just(new UsernamePasswordAuthenticationToken(
        new User(source.getSubject()), source, Collections.emptyList()))
    );
    return provider;
  }

  @Bean
  @Primary
  public ReactiveAuthenticationManager reactiveAuthenticationManager(UserService userService) {
    return new UserDetailsRepositoryReactiveAuthenticationManager(userService);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfig = new CorsConfiguration();

    corsConfig.setAllowedOrigins(List.of(corsProperties.allowedOrigins()));
    corsConfig.setMaxAge(corsProperties.maxAge());
    corsConfig.setAllowCredentials(corsProperties.allowedCredentials());
    corsConfig.setAllowedMethods(List.of(corsProperties.allowedMethods()));
    corsConfig.setAllowedHeaders(List.of(corsProperties.allowedHeaders()));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }
}
