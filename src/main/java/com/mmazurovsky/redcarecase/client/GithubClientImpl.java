package com.mmazurovsky.redcarecase.client;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

@Component
public class GithubClientImpl implements GithubClient {
    private final WebClient githubWebClient;

    public GithubClientImpl(WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    @Override
    public Mono<GithubRepositorySearchResponse> searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage
    ) {
        // Construct the query string
        final String queryString = buildQueryString(request);

        // Build URI with query parameters
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
                .bodyToMono(GithubRepositorySearchResponse.class);
    }

    private String buildQueryString(RepositoriesSearchIn request) {
        // Create query builder
        final StringBuilder queryBuilder = new StringBuilder();

        // Add search keywords
        queryBuilder.append(request.keywords());

        // Add language filter
        queryBuilder.append("+language:").append(request.language());

        // Add created date filter
        final String formattedDate = request.earliestCreatedDate()
                .format(DateTimeFormatter.ISO_DATE);
        queryBuilder.append("+created:>=").append(formattedDate);

        return queryBuilder.toString();
    }
}