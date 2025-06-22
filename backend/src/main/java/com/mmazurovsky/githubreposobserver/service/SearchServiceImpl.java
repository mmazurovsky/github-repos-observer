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
import com.mmazurovsky.githubreposobserver.dto.GithubRepositorySearchResults;
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
    public GithubRepositorySearchResults searchRepositories(RepositoriesSearchIn request) {
        int maxPages = request.maxPages() != null ? request.maxPages() : 5;

        // Create futures for the 4 different search requests
        CompletableFuture<Integer> minStarsFuture = CompletableFuture.supplyAsync(() ->
            getMinStars(request), virtualThreadExecutor);

        CompletableFuture<Integer> minForksFuture = CompletableFuture.supplyAsync(() ->
            getMinForks(request), virtualThreadExecutor);

        CompletableFuture<Integer> maxStarsFuture = CompletableFuture.supplyAsync(() ->
            getMaxStars(request), virtualThreadExecutor);

        CompletableFuture<List<GithubRepositoryItemResponse>> repositoriesFuture = CompletableFuture.supplyAsync(() ->
            getRepositoriesWithMaxForks(request, maxPages), virtualThreadExecutor);

        // Wait for all futures to complete - let any exceptions propagate
        int minStars = minStarsFuture.join();
        int minForks = minForksFuture.join();
        int maxStars = maxStarsFuture.join();
        List<GithubRepositoryItemResponse> repositories = repositoriesFuture.join();

        // Get maxForks from the repositories we fetched (desc order by forks)
        int maxForks = repositories.isEmpty() ? 0 : repositories.get(0).forksCount();

        logger.info("Search results: minStars={}, maxStars={}, minForks={}, maxForks={}, repositories={}",
                   minStars, maxStars, minForks, maxForks, repositories.size());

        return new GithubRepositorySearchResults(minStars, maxStars, minForks, maxForks, repositories);
    }

    private int getMinStars(RepositoriesSearchIn request) {
        // 1. order is asc, sort is stars - to get minStars from first item
        RepositoriesSearchIn minStarsRequest = new RepositoriesSearchIn(
            request.keywords(),
            request.earliestCreatedDate(),
            request.language(),
            1 // Only need 1 page
        );

        GithubRepositorySearchResponse response = githubRepositoryClient.searchRepositories(
            minStarsRequest, 1, 1, "stars", "asc"
        );

        if (!response.items().isEmpty()) {
            int minStars = response.items().get(0).stargazersCount();
            logger.debug("MinStars found: {}", minStars);
            return minStars;
        }
        return 0;
    }

    private int getMinForks(RepositoriesSearchIn request) {
        // 2. order is asc, sort is forks - to get minForks from first item
        RepositoriesSearchIn minForksRequest = new RepositoriesSearchIn(
            request.keywords(),
            request.earliestCreatedDate(),
            request.language(),
            1 // Only need 1 page
        );

        GithubRepositorySearchResponse response = githubRepositoryClient.searchRepositories(
            minForksRequest, 1, 1, "forks", "asc"
        );

        if (!response.items().isEmpty()) {
            int minForks = response.items().get(0).forksCount();
            logger.debug("MinForks found: {}", minForks);
            return minForks;
        }
        return 0;
    }

    private int getMaxStars(RepositoriesSearchIn request) {
        // 3. order is desc, sort is stars - to get maxStars from first item
        RepositoriesSearchIn maxStarsRequest = new RepositoriesSearchIn(
            request.keywords(),
            request.earliestCreatedDate(),
            request.language(),
            1 // Only need 1 page
        );

        GithubRepositorySearchResponse response = githubRepositoryClient.searchRepositories(
            maxStarsRequest, 1, 1, "stars", "desc"
        );

        if (!response.items().isEmpty()) {
            int maxStars = response.items().get(0).stargazersCount();
            logger.debug("MaxStars found: {}", maxStars);
            return maxStars;
        }
        return 0;
    }

    private List<GithubRepositoryItemResponse> getRepositoriesWithMaxForks(RepositoriesSearchIn request, int maxPages) {
        // 4. order is desc, sort is forks - fetch maxPages amount of pages
        List<CompletableFuture<List<GithubRepositoryItemResponse>>> pageFutures = IntStream.rangeClosed(1, maxPages)
            .mapToObj(page -> CompletableFuture.supplyAsync(() -> {
                GithubRepositorySearchResponse response = githubRepositoryClient.searchRepositories(
                    request, page, RESULTS_PER_PAGE, "forks", "desc"
                );
                return response.items();
            }, virtualThreadExecutor))
            .toList();

        // Collect all results - let any exceptions propagate
        List<GithubRepositoryItemResponse> allItems = new ArrayList<>();
        for (CompletableFuture<List<GithubRepositoryItemResponse>> future : pageFutures) {
            allItems.addAll(future.join());
        }

        logger.debug("Repositories fetched: {}", allItems.size());
        return allItems;
    }
}
