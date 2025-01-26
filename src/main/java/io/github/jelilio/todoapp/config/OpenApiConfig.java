package io.github.jelilio.todoapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
@OpenAPIDefinition(info = @Info(title = "Todo APIs", description = "Todo APIs v1.0", version = "v1"))
@SecurityScheme(
    name = "bearer_auth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "basic"
)
public class OpenApiConfig {
}
