plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mmazurovsky"
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
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Netty client
    implementation("io.projectreactor.netty:reactor-netty-http")

    // JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations")
    // Retries logic
    implementation("org.springframework.retry:spring-retry")
    // Logging
    implementation("org.slf4j:slf4j-api")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    // Other
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.107.Final:osx-aarch_64")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
