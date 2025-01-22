package io.github.jelilio.todoapp.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(TokenProperties access, TokenProperties refresh) {
  public record TokenProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) { }
}
