package com.mmazurovsky.githubreposobserver.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.mmazurovsky.githubreposobserver.util.ClientLoggingUtil.logRequest;

@Configuration
public class WebClientConfig {

    @Value("${github.api.token}")
    private String githubToken;

    @Bean
    public WebClient githubWebClient() {

        ConnectionProvider provider = ConnectionProvider.builder("github-conn-provider")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2MB
                )
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .filter(logRequest())
                .build();
    }
}