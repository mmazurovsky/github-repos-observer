plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mmazurovsky"
version = "0.0.2-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // HTTP client for Java 21
    implementation("org.springframework.boot:spring-boot-starter-webflux") // Keep for WebClient only

    // JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations")
    // Retries logic
    implementation("org.springframework.retry:spring-retry")
    // Rate limiting
    implementation("com.google.guava:guava:33.0.0-jre")
    // Logging
    implementation("org.slf4j:slf4j-api")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Other
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.107.Final:osx-aarch_64")
    implementation("org.jetbrains:annotations:24.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
