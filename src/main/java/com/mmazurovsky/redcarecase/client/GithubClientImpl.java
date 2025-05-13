package com.mmazurovsky.redcarecase.client;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.redcarecase.service.SearchServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import org.springframework.http.HttpStatus;


import java.time.Duration;
import java.time.format.DateTimeFormatter;

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
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("4xx error from GitHub for page {}: {}", page, errorBody);
                                    return clientResponse.createException()
                                            .flatMap(Mono::error);
                                })
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    logger.error("5xx error from GitHub for page {}: {}", page, errorBody);
                                    return clientResponse.createException()
                                            .flatMap(Mono::error);
                                })
                )
                .bodyToMono(GithubRepositorySearchResponse.class)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(200))
                                .filter(throwable -> throwable instanceof WebClientResponseException &&
                                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                                .onRetryExhaustedThrow((spec, signal) ->
                                        new RuntimeException("Retries exhausted after server errors: " + signal.failure().getMessage(), signal.failure())
                                )
                                .doBeforeRetry(signal ->
                                        logger.warn("Retrying page {} due to: {}", page, signal.failure().getMessage())
                                )
                )
                .timeout(Duration.ofSeconds(1)) // total max duration per call including retries
                .doOnError(error ->
                        logger.error("Request failed for page {}: {}", page, error.getMessage())
                );
    }


    private String buildQueryString(RepositoriesSearchIn request) {
        // Create query builder
        final StringBuilder queryBuilder = new StringBuilder();

        // Add search keywords
        queryBuilder.append(request.keywords());

        if (request.language().isPresent()) {
            // Add language filter
            queryBuilder.append("+language:").append(request.language());
        }

        final var earliestCreatedDate = request.earliestCreatedDate();
        if (earliestCreatedDate.isPresent()) {
            // Add created date filter
            final String formattedDate = earliestCreatedDate.get()
                    .format(DateTimeFormatter.ISO_DATE);
            queryBuilder.append("+created:>=").append(formattedDate);
        }

        return queryBuilder.toString();
    }
}