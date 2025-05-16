package com.mmazurovsky.redcarecase.client;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.http.HttpStatus;


import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

@Component
public class GithubClientImpl implements GithubClient {
    private final WebClient githubWebClient;

    private static final Logger logger = LoggerFactory.getLogger(GithubClientImpl.class);

    public GithubClientImpl(WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    @Override
    public Mono<GithubRepositorySearchResponse> searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage
    ) {
        final String queryString = buildQueryString(request);

        return githubWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", queryString)
                        .queryParam("sort", "stars")
                        .queryParam("order", "desc")
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build(true))
                .exchangeToMono(clientResponse -> {
                    final var status = clientResponse.statusCode();

                    if (status.value() == 422) {
                        // Page can't be processed → treat as no result
                        return Mono.empty();
                    } else if (status.is4xxClientError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("4xx error from GitHub for page {}: {}", page, errorBody);
                                    return Mono.error(new ResponseStatusException(
                                            HttpStatus.INTERNAL_SERVER_ERROR,
                                            "Application error when requesting GitHub API"
                                    ));
                                });
                    } else if (status.is5xxServerError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("5xx error from GitHub for page {}: {}", page, errorBody);
                                    return clientResponse.createException().flatMap(Mono::error);
                                });
                    } else {
                        return clientResponse.bodyToMono(GithubRepositorySearchResponse.class);
                    }
                })
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(400))
                                .filter(throwable -> throwable instanceof WebClientResponseException &&
                                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                                .onRetryExhaustedThrow((spec, signal) ->
                                        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Retries exhausted after server errors: " + signal.failure().getMessage(),
                                                signal.failure()))
                                .doBeforeRetry(signal ->
                                        logger.warn("Retrying page {} due to: {}", page, signal.failure().getMessage()))
                )
                .onErrorMap(TimeoutException.class,
                        te -> new ResponseStatusException(
                                HttpStatus.GATEWAY_TIMEOUT, "GitHub request timed‑out", te))
                .onErrorMap(WebClientRequestException.class,
                        ce -> new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE, "GitHub connection error", ce))
                .onErrorMap(ex -> !(ex instanceof ResponseStatusException),
                        ex -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "GitHub client failure", ex));
    }

    private String buildQueryString(RepositoriesSearchIn request) {
        // Create query builder
        final StringBuilder queryBuilder = new StringBuilder();

        // Add search keywords
        queryBuilder.append(request.keywords());

        if (request.language() != null) {
            // Add language filter
            queryBuilder.append("+language:").append(request.language());
        }

        final var earliestCreatedDate = request.earliestCreatedDate();
        if (earliestCreatedDate != null) {
            // Add created date filter
            final String formattedDate = earliestCreatedDate
                    .format(DateTimeFormatter.ISO_DATE);
            queryBuilder.append("+created:>=").append(formattedDate);
        }

        return queryBuilder.toString();
    }
}