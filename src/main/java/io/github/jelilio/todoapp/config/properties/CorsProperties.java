package io.github.jelilio.todoapp.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cors", ignoreUnknownFields = false)
public record CorsProperties(
    Long maxAge,
    String[] allowedOrigins,
    String[] allowedMethods,
    String[] allowedHeaders,
    Boolean allowedCredentials
) {
}
