package com.mmazurovsky.githubreposobserver.service;

import java.time.Duration;
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
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private static final int RESULTS_PER_PAGE = 100;
    private static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(50);
    private static final int DEFAULT_MAX_PAGES = 5;

    private final GithubClient githubClient;
    private final ExecutorService virtualThreadExecutor;

    public SearchServiceImpl(GithubClient githubClient) {
        this.githubClient = githubClient;
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public GithubRepositorySearchResults searchRepositories(RepositoriesSearchIn request) {
        int maxPages = request.maxPages() != null ? request.maxPages() : DEFAULT_MAX_PAGES;

        logger.info("🔍 Searching repositories with hybrid approach: sequential normalization + concurrent pagination");

        var normalizationValues = fetchNormalizationValues(request);
        var repositories = fetchRepositoriesConcurrently(request, maxPages);

        int maxForks = repositories.stream()
                .mapToInt(GithubRepositoryItemResponse::forksCount)
                .max()
                .orElse(0);

        logger.info("✅ Search completed: minStars={}, maxStars={}, minForks={}, maxForks={}, repositories={}",
                normalizationValues.minStars(), normalizationValues.maxStars(),
                normalizationValues.minForks(), maxForks, repositories.size());

        return new GithubRepositorySearchResults(
                normalizationValues.minStars(),
                normalizationValues.maxStars(),
                normalizationValues.minForks(),
                maxForks,
                repositories
        );
    }

    private NormalizationValues fetchNormalizationValues(RepositoriesSearchIn request) {
        logger.debug("📊 Fetching normalization values sequentially with rate limiting...");

        int minStars = fetchSingleValue(request, "stars", "asc", "min stars");
        rateLimitDelay();

        int minForks = fetchSingleValue(request, "forks", "asc", "min forks");
        rateLimitDelay();

        int maxStars = fetchSingleValue(request, "stars", "desc", "max stars");
        rateLimitDelay();

        return new NormalizationValues(minStars, maxStars, minForks);
    }

    private List<GithubRepositoryItemResponse> fetchRepositoriesConcurrently(RepositoriesSearchIn request, int maxPages) {
        logger.debug("🚀 Fetching {} pages concurrently using virtual threads", maxPages);

        return IntStream.rangeClosed(1, maxPages)
                .mapToObj(page -> fetchPageAsync(request, page))
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    }

    private CompletableFuture<List<GithubRepositoryItemResponse>> fetchPageAsync(RepositoriesSearchIn request, int page) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("📄 Fetching page {} using virtual thread", page);

            var response = githubClient.searchRepositories(request, page, RESULTS_PER_PAGE, "forks", "desc");

            logger.debug("✓ Completed page {} with {} items", page, response.items().size());
            return response.items();
        }, virtualThreadExecutor);
    }

        private int fetchSingleValue(RepositoriesSearchIn request, String sortBy, String order, String description) {
        var singlePageRequest = new RepositoriesSearchIn(
            request.keywords(),
            request.earliestCreatedDate(),
            request.language(),
            1 // Only need 1 page for normalization values
    );
        var response = githubClient.searchRepositories(singlePageRequest, 1, 1, sortBy, order);

        if (response.items().isEmpty()) {
            logger.debug("⚠️ No results found for {}", description);
            return 0;
        }

        int value = extractValue(response.items().get(0), sortBy);
        logger.debug("📈 {} found: {}", description, value);
        return value;
    }

    private int extractValue(GithubRepositoryItemResponse item, String sortBy) {
        return switch (sortBy) {
            case "stars" -> item.stargazersCount();
            case "forks" -> item.forksCount();
            default -> throw new IllegalArgumentException("Unknown sort field: " + sortBy);
        };
    }

    private void rateLimitDelay() {
        try {
            Thread.sleep(RATE_LIMIT_DELAY.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("⚠️ Rate limit delay interrupted, continuing without delay");
        }
    }

    private record NormalizationValues(int minStars, int maxStars, int minForks) {}
}
