package com.mmazurovsky.githubreposobserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

public class ClientLoggingUtil {
    private static final Logger logger = LoggerFactory.getLogger(ClientLoggingUtil.class);

    public static ExchangeFilterFunction logRequest() {
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
