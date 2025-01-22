package io.github.jelilio.todoapp;


import io.github.jelilio.todoapp.config.properties.AuthProperties;
import io.github.jelilio.todoapp.config.properties.CorsProperties;
import io.github.jelilio.todoapp.config.properties.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;


@SpringBootApplication
@EnableReactiveMongoAuditing
@EnableConfigurationProperties({CorsProperties.class, AuthProperties.class, RsaKeyProperties.class})
public class TodoappApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoappApplication.class, args);
	}

}
