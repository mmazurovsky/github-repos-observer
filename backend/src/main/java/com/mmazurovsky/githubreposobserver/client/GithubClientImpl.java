package com.mmazurovsky.githubreposobserver.client;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

@Component
public class GithubClientImpl implements GithubClient {
    private final WebClient githubWebClient;

    private static final Logger logger = LoggerFactory.getLogger(GithubClientImpl.class);

    public GithubClientImpl(WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    @Override
    public GithubRepositorySearchResponse searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage
    ) {
        final String queryString = buildQueryString(request);

        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount <= maxRetries) {
            try {
                return githubWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search/repositories")
                                .queryParam("q", queryString)
                                .queryParam("sort", "stars")
                                .queryParam("order", "desc")
                                .queryParam("page", page)
                                .queryParam("per_page", perPage)
                                .build(true))
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 422,
                                response -> response.bodyToMono(String.class).flatMap(body -> {
                                    logger.warn("Page {} can't be processed (422), returning empty result", page);
                                    return reactor.core.publisher.Mono.error(new ResponseStatusException(HttpStatus.NO_CONTENT, "Page can't be processed"));
                                })
                        )
                        .onStatus(
                                status -> status.is4xxClientError(),
                                response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                                    logger.error("4xx error from GitHub for page {}: {}", page, errorBody);
                                    return reactor.core.publisher.Mono.error(new ResponseStatusException(
                                            HttpStatus.INTERNAL_SERVER_ERROR,
                                            "Application error when requesting GitHub API"
                                    ));
                                })
                        )
                        .onStatus(
                                status -> status.is5xxServerError(),
                                response -> response.bodyToMono(String.class).flatMap(errorBody -> {
                                    logger.error("5xx error from GitHub for page {}: {}", page, errorBody);
                                    return reactor.core.publisher.Mono.error(new WebClientResponseException(
                                            response.statusCode().value(),
                                            "GitHub server error",
                                            null,
                                            errorBody.getBytes(),
                                            null
                                    ));
                                })
                        )
                        .bodyToMono(GithubRepositorySearchResponse.class)
                        .block(Duration.ofSeconds(30));

            } catch (WebClientResponseException ex) {
                if (ex.getStatusCode().value() == 422) {
                    // Return empty response for 422 errors
                    logger.warn("Page {} can't be processed (422), returning empty result", page);
                    return new GithubRepositorySearchResponse(0, false, List.of());
                }

                if (ex.getStatusCode().is5xxServerError() && retryCount < maxRetries) {
                    retryCount++;
                    logger.warn("Retrying page {} due to server error (attempt {}/{}): {}",
                               page, retryCount, maxRetries, ex.getMessage());
                    try {
                        Thread.sleep(400 * retryCount); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Request interrupted", ie);
                    }
                    continue;
                }

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "GitHub API error: " + ex.getMessage(),
                        ex
                );
            } catch (WebClientRequestException ex) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "GitHub connection error",
                        ex
                );
            } catch (Exception ex) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "GitHub client failure",
                        ex
                );
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Retries exhausted after server errors for page " + page
        );
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
