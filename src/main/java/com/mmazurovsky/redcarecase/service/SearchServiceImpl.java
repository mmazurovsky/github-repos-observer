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
        // INFO: use 5 as default
        final int maxPages = request.maxPages() != null ? request.maxPages() : 5;

        return Flux.create(sink -> {
            List<GithubRepositoryItemResponse> buffer = new ArrayList<>();
            fetchPageRecursively(request, 1, maxPages, sink, buffer);
        });
    }

    private void fetchPageRecursively(RepositoriesSearchIn request,
                                      int page,
                                      int maxPages,
                                      FluxSink<GithubRepositoryItemResponse> sink,
                                      List<GithubRepositoryItemResponse> buffer) {

        if (page > maxPages || sink.isCancelled()) {
            sink.complete();
            return;
        }

        githubRepositoryClient.searchRepositories(request, page, RESULTS_PER_PAGE)
                .subscribe(response -> {
                    // Mono was not empty
                    if (!response.items().isEmpty()) {
                        List<GithubRepositoryItemResponse> items = response.items();
                        buffer.addAll(items);
                        items.forEach(sink::next);

                        // Recurse to next page
                        fetchPageRecursively(request, page + 1, maxPages, sink, buffer);
                    } else {
                        sink.complete(); // Empty page: stop
                    }
                }, error -> {
                    logger.error("Error when fetching page {}, error: {}", page, error.toString());

                    if (!buffer.isEmpty()) {
                        sink.complete(); // Return what we have
                    } else {
                        sink.error(error); // No data at all — propagate error
                    }
                }, () -> {
                    // onComplete: triggered when Mono is empty
                    sink.complete();
                });
    }

}
