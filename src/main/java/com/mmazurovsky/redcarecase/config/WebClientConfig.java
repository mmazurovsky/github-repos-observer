package com.mmazurovsky.redcarecase.config;

import com.mmazurovsky.redcarecase.service.SearchServiceImpl;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${github.api.token}")
    private String githubToken;

    @Bean
    public WebClient githubWebClient() {

        ConnectionProvider provider = ConnectionProvider.builder("github-conn-provider")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20000) // 10 seconds
                .responseTimeout(Duration.ofSeconds(20))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(20, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(20, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) // 2MB
                )
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder log = new StringBuilder();
            log.append("→ ")
                    .append(clientRequest.method())
                    .append(" ")
                    .append(clientRequest.url());

            clientRequest.headers().forEach((name, values) -> {
                if (!name.equalsIgnoreCase("Authorization")) {
                    log.append("\n  ").append(name).append(": ").append(String.join(",", values));
                } else {
                    log.append("\n  ").append(name).append(": [REDACTED]");
                }
            });

            logger.debug(log.toString());
            return Mono.just(clientRequest);
        });
    }
}