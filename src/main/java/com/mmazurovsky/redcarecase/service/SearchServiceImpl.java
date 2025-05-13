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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;

@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private static final int RESULTS_PER_PAGE = 100;

    private final GithubClient githubRepositoryClient;

    public SearchServiceImpl(GithubClient githubRepositoryClient) {
        this.githubRepositoryClient = githubRepositoryClient;
    }

    @Override
    public Flux<GithubRepositoryItemResponse> searchRepositories(RepositoriesSearchIn request) {
        final int maxPages = request.maxPages().orElse(5);

        return Flux.range(1, maxPages)
                .concatMap(page ->
                        githubRepositoryClient.searchRepositories(request, page, RESULTS_PER_PAGE)
                                .flatMapMany(response -> {
                                    if (response.incompleteResults()) {
                                        logger.warn("Incomplete results for page {}", page);
                                    }

                                    final var items = response.items();

                                    if (items.isEmpty()) {
                                        return Flux.empty();
                                    } else {
                                        return Flux.fromIterable(items);
                                    }
                                })
                                .onErrorResume(error -> {
                                    logger.error("Failed to fetch page {} after retries: {}", page, error.getMessage());
                                    return Mono.error(error);
                                })
                );
    }
}
