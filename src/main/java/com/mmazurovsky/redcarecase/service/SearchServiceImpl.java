package com.mmazurovsky.redcarecase.service;

import com.mmazurovsky.redcarecase.client.GithubClient;
import com.mmazurovsky.redcarecase.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;

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
        int maxPages = request.maxPages() != null ? request.maxPages() : 5;

        return Flux.range(1, maxPages)
                .concatMap(page ->
                        githubRepositoryClient.searchRepositories(request, page, RESULTS_PER_PAGE)
                                .flatMapMany(response -> Flux.fromIterable(response.items()))
                                .onErrorResume(error -> {
                                    logger.error("Failed to fetch page {}: {}", page, error.getMessage());
                                    return Flux.error(error);
                                })
                );
    }
}
