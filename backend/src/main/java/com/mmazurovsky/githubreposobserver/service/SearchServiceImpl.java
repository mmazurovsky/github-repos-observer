package com.mmazurovsky.githubreposobserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mmazurovsky.githubreposobserver.client.GithubClient;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private static final int RESULTS_PER_PAGE = 100;

    private final GithubClient githubRepositoryClient;
    private final ExecutorService virtualThreadExecutor;

    public SearchServiceImpl(GithubClient githubRepositoryClient) {
        this.githubRepositoryClient = githubRepositoryClient;
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public List<GithubRepositoryItemResponse> searchRepositories(RepositoriesSearchIn request) {
        int maxPages = request.maxPages() != null ? request.maxPages() : 5;

        // Use virtual threads to fetch all pages concurrently
        List<CompletableFuture<List<GithubRepositoryItemResponse>>> pageFutures = IntStream.rangeClosed(1, maxPages)
                .mapToObj(page -> CompletableFuture.supplyAsync(() -> {
                    try {
                        GithubRepositorySearchResponse response = githubRepositoryClient.searchRepositories(request, page, RESULTS_PER_PAGE);
                        return response.items();
                    } catch (Exception error) {
                        logger.error("Failed to fetch page {}: {}", page, error.getMessage());
                        return List.<GithubRepositoryItemResponse>of(); // Return empty list on error
                    }
                }, virtualThreadExecutor))
                .toList();

        // Collect all results
        List<GithubRepositoryItemResponse> allItems = new ArrayList<>();
        for (CompletableFuture<List<GithubRepositoryItemResponse>> future : pageFutures) {
            try {
                allItems.addAll(future.join());
            } catch (Exception error) {
                logger.error("Error collecting page results: {}", error.getMessage());
            }
        }

        return allItems;
    }
}
