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

    // Static exception variables with generic messages - never expose internal details to clients
    public static final ResponseStatusException GITHUB_4XX_CLIENT_ERROR_EXCEPTION =
        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid search request");

    public static final ResponseStatusException GITHUB_5XX_SERVER_ERROR_EXCEPTION =
        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Search service temporarily unavailable");

    public static final ResponseStatusException GITHUB_CONNECTION_ERROR_EXCEPTION =
        new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cannot connect to search service");

    public static final ResponseStatusException GITHUB_CLIENT_FAILURE_EXCEPTION =
        new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Search operation failed");

    private final WebClient githubWebClient;

    private static final Logger logger = LoggerFactory.getLogger(GithubClientImpl.class);

    public GithubClientImpl(WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    @Override
    public GithubRepositorySearchResponse searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage,
            String sort,
            String order
    ) {
        final String queryString = buildQueryString(request);

        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                return githubWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search/repositories")
                                .queryParam("q", queryString)
                                .queryParam("sort", sort)
                                .queryParam("order", order)
                                .queryParam("page", page)
                                .queryParam("per_page", perPage)
                                .build(true))
                        .retrieve()
                        .bodyToMono(GithubRepositorySearchResponse.class)
                        .block(Duration.ofSeconds(30));

            } catch (WebClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();

                if (statusCode == 422) {
                    // Return empty response for 422 errors - this is expected behavior
                    logger.info("Page {} returned 422 (Unprocessable Entity), returning empty result gracefully", page);
                    return new GithubRepositorySearchResponse(0, false, List.of());
                }

                if (ex.getStatusCode().is4xxClientError()) {
                    logger.error("4xx error from GitHub for page {}: status={}, message={}", page, statusCode, ex.getMessage());
                    throw GITHUB_4XX_CLIENT_ERROR_EXCEPTION;
                }

                if (ex.getStatusCode().is5xxServerError() && retryCount < maxRetries) {
                    retryCount++;
                    logger.warn("Retrying page {} due to server error (attempt {}/{}): status={}, message={}",
                               page, retryCount, maxRetries, statusCode, ex.getMessage());
                    try {
                        Thread.sleep(400 * retryCount); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Request interrupted during retry backoff for page {}, returning empty result gracefully", page);
                        // Return empty response instead of throwing exception
                        return new GithubRepositorySearchResponse(0, false, List.of());
                    }
                    continue;
                }

                // For 5xx server errors when retries are exhausted
                logger.error("5xx server error from GitHub for page {} (retries exhausted): status={}, message={}",
                           page, statusCode, ex.getMessage());
                throw GITHUB_5XX_SERVER_ERROR_EXCEPTION;
            } catch (WebClientRequestException ex) {
                logger.error("WebClientRequestException occurred for page {}: {}", page, ex.getMessage());
                throw GITHUB_CONNECTION_ERROR_EXCEPTION;
            } catch (Exception ex) {
                logger.error("Unexpected exception occurred for page {}: {}", page, ex.getMessage(), ex);
                throw GITHUB_CLIENT_FAILURE_EXCEPTION;
            }
        }

        logger.warn("All retries exhausted for page {} after {} attempts, returning empty result gracefully", page, maxRetries);
        // Return empty response instead of throwing exception for retry exhaustion
        return new GithubRepositorySearchResponse(0, false, List.of());
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

