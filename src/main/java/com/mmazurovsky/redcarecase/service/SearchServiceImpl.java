package com.mmazurovsky.redcarecase.service;

import com.mmazurovsky.redcarecase.client.GithubClient;
import com.mmazurovsky.redcarecase.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private static final int MAX_PAGES = 10; // Prevent infinite pagination
    private static final int RESULTS_PER_PAGE = 100;

    private final GithubClient githubRepositoryClient;

    public SearchServiceImpl(GithubClient githubRepositoryClient) {
        this.githubRepositoryClient = githubRepositoryClient;
    }

    @Override
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public Flux<GithubRepositoryItemResponse> searchRepositories(RepositoriesSearchIn request) {
        return Flux.range(1, MAX_PAGES)
                .concatMap(page ->
                        githubRepositoryClient.searchRepositories(request, page, RESULTS_PER_PAGE)
                                .flatMapMany(response -> {
                                    // Log if results are incomplete
                                    if (response.incompleteResults()) {
                                        logger.warn("Incomplete results for page {}", page);
                                    }

                                    // If no items, return empty flux
                                    if (response.items() == null || response.items().isEmpty()) {
                                        return Flux.empty();
                                    }

                                    // Transform and enrich each repository item
                                    return Flux.fromIterable(response.items())
                                            .flatMap(item ->
                                                    // Enrich each repository item
                                                    Mono.just(item)
                                            );
                                })
                                // Stop if no more results
                                .takeUntil(item -> false)
                );
    }
}
