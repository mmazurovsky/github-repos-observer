package com.mmazurovsky.githubreposobserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mmazurovsky.githubreposobserver.client.GithubClient;
import com.mmazurovsky.githubreposobserver.dto.GithubRepositorySearchResults;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImplTest.class);

    @Mock
    private GithubClient githubClient;

    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl(githubClient);
    }

    @Test
    void searchRepositories_shouldExecuteConcurrently() {
        // Given
        final int maxPages = 3; // Use fewer pages for easier test management
        final int delayPerPageMs = 200; // Reasonable delay for testing
        final int expectedMaxExecutionTimeMs = delayPerPageMs + 150; // Allow overhead for parallel execution

        RepositoriesSearchIn request = new RepositoriesSearchIn(
                "test-keywords",
                null,
                null,
                maxPages
        );

        // Counter to track concurrent calls for the main search (desc forks)
        AtomicInteger concurrentCalls = new AtomicInteger(0);
        AtomicInteger maxConcurrentCalls = new AtomicInteger(0);

        // Mock the 4 different API calls:

        // 1. Min stars (asc stars) - returns repo with lowest stars
        when(githubClient.searchRepositories(any(RepositoriesSearchIn.class), eq(1), eq(1), eq("stars"), eq("asc")))
                .thenReturn(new GithubRepositorySearchResponse(
                        1,
                        false,
                        List.of(new GithubRepositoryItemResponse(
                                1L,
                                "min-stars-repo",
                                "owner/min-stars-repo",
                                "https://github.com/owner/min-stars-repo",
                                10, // min stars
                                5,
                                "2023-01-01T00:00:00Z",
                                "Java",
                                OffsetDateTime.parse("2023-01-01T00:00:00Z")
                        ))
                ));

        // 2. Min forks (asc forks) - returns repo with lowest forks
        when(githubClient.searchRepositories(any(RepositoriesSearchIn.class), eq(1), eq(1), eq("forks"), eq("asc")))
                .thenReturn(new GithubRepositorySearchResponse(
                        1,
                        false,
                        List.of(new GithubRepositoryItemResponse(
                                2L,
                                "min-forks-repo",
                                "owner/min-forks-repo",
                                "https://github.com/owner/min-forks-repo",
                                20,
                                1, // min forks
                                "2023-01-01T00:00:00Z",
                                "Python",
                                OffsetDateTime.parse("2023-01-01T00:00:00Z")
                        ))
                ));

        // 3. Max stars (desc stars) - returns repo with highest stars
        when(githubClient.searchRepositories(any(RepositoriesSearchIn.class), eq(1), eq(1), eq("stars"), eq("desc")))
                .thenReturn(new GithubRepositorySearchResponse(
                        1,
                        false,
                        List.of(new GithubRepositoryItemResponse(
                                3L,
                                "max-stars-repo",
                                "owner/max-stars-repo",
                                "https://github.com/owner/max-stars-repo",
                                10000, // max stars
                                2000,
                                "2023-01-01T00:00:00Z",
                                "TypeScript",
                                OffsetDateTime.parse("2023-01-01T00:00:00Z")
                        ))
                ));

        // 4. Main search (desc forks) - returns multiple pages of repositories
        when(githubClient.searchRepositories(eq(request), anyInt(), eq(100), eq("forks"), eq("desc")))
                .thenAnswer(invocation -> {
                    int currentConcurrent = concurrentCalls.incrementAndGet();
                    maxConcurrentCalls.updateAndGet(max -> Math.max(max, currentConcurrent));

                    int page = invocation.getArgument(1);
                    logger.debug("Starting to fetch page {} (concurrent calls: {})", page, currentConcurrent);

                    try {
                        // Simulate network delay
                        Thread.sleep(delayPerPageMs);

                        // Create items for this page
                        List<GithubRepositoryItemResponse> pageItems = List.of(
                                new GithubRepositoryItemResponse(
                                        (long) (page * 100 + 1),
                                        "repo-" + page + "-1",
                                        "owner/repo-" + page + "-1",
                                        "https://github.com/owner/repo-" + page + "-1",
                                        5000 - (page * 100),
                                        3000 - (page * 10),
                                        "2023-01-01T00:00:00Z",
                                        "Java",
                                        OffsetDateTime.parse("2023-01-01T00:00:00Z")
                                ),
                                new GithubRepositoryItemResponse(
                                        (long) (page * 100 + 2),
                                        "repo-" + page + "-2",
                                        "owner/repo-" + page + "-2",
                                        "https://github.com/owner/repo-" + page + "-2",
                                        4500 - (page * 100),
                                        2900 - (page * 10),
                                        "2023-01-01T00:00:00Z",
                                        "Python",
                                        OffsetDateTime.parse("2023-01-01T00:00:00Z")
                                )
                        );

                        return new GithubRepositorySearchResponse(
                                pageItems.size(),
                                false,
                                pageItems
                        );
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } finally {
                        concurrentCalls.decrementAndGet();
                        logger.debug("Finished fetching page {}", page);
                    }
                });

        // When
        long startTime = System.currentTimeMillis();
        GithubRepositorySearchResults results = searchService.searchRepositories(request);
        long executionTime = System.currentTimeMillis() - startTime;

        // Then - Focus only on concurrency testing
        logger.info("🚀 CONCURRENCY TEST 🚀");
        logger.info("Execution time: {}ms (expected max: {}ms for parallel execution)",
                   executionTime, expectedMaxExecutionTimeMs);
        logger.info("Max concurrent calls observed: {} (expected: {})",
                   maxConcurrentCalls.get(), maxPages);
        logger.info("Total repositories returned: {}", results.repositories().size());

        // Verify parallel execution happened for the main search
        assertThat(maxConcurrentCalls.get())
                .as("Should have concurrent execution for the main search")
                .isGreaterThanOrEqualTo(2); // At least 2 concurrent calls

        // Verify execution time indicates parallel processing
        assertThat(executionTime)
                .as("Execution time should indicate parallel execution")
                .isLessThan(expectedMaxExecutionTimeMs);

        // Verify we got some repositories back (basic functionality check)
        assertThat(results.repositories())
                .as("Should return some repositories")
                .isNotEmpty();
    }
}
