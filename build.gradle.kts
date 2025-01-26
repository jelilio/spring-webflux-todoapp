plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.jelilio"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.apache.commons:commons-lang3:3.12.0")

	implementation("io.swagger.core.v3:swagger-annotations:2.2.23")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.8.3")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.3")

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//	testImplementation("org.testcontainers:testcontainers:1.20.4")
//	testImplementation("org.testcontainers:junit-jupiter:1.20.4")
//	testImplementation("org.testcontainers:mongodb:1.20.4")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
