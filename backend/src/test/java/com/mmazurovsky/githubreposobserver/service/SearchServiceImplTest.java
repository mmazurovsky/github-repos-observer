package com.mmazurovsky.githubreposobserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

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
    void searchRepositories_shouldFetchPagesInParallel() {
        // Given
        final int maxPages = 20; // Increased from 5 to 20 for more dramatic parallel execution
        final int itemsPerPage = 3; // Increased items per page to simulate more realistic GitHub responses
        final int expectedTotalItems = maxPages * itemsPerPage; // 20 × 3 = 60 items
        final int delayPerPageMs = 300; // Increased delay to make timing more measurable
        final int expectedMaxExecutionTimeMs = delayPerPageMs + 200; // Allow overhead for parallel execution

        RepositoriesSearchIn request = new RepositoriesSearchIn(
                "test-keywords",
                null,
                null,
                maxPages
        );

        // Counter to track concurrent calls
        AtomicInteger concurrentCalls = new AtomicInteger(0);
        AtomicInteger maxConcurrentCalls = new AtomicInteger(0);

        // Mock GitHub client to simulate network delay and track concurrency
        when(githubClient.searchRepositories(any(RepositoriesSearchIn.class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    int currentConcurrent = concurrentCalls.incrementAndGet();
                    maxConcurrentCalls.updateAndGet(max -> Math.max(max, currentConcurrent));

                    int page = invocation.getArgument(1);
                    logger.debug("Starting to fetch page {} (concurrent calls: {})", page, currentConcurrent);

                    try {
                        // Simulate network delay
                        Thread.sleep(delayPerPageMs);

                        // Create multiple items for this page to simulate realistic GitHub response
                        List<GithubRepositoryItemResponse> pageItems = List.of(
                                new GithubRepositoryItemResponse(
                                        (long) (page * 100 + 1),
                                        "repo-" + page + "-1",
                                        "owner/repo-" + page + "-1",
                                        "https://github.com/owner/repo-" + page + "-1",
                                        1000 + page * 10,
                                        500 + page * 5,
                                        "2023-01-01T00:00:00Z",
                                        "Java",
                                        null
                                ),
                                new GithubRepositoryItemResponse(
                                        (long) (page * 100 + 2),
                                        "repo-" + page + "-2",
                                        "owner/repo-" + page + "-2",
                                        "https://github.com/owner/repo-" + page + "-2",
                                        2000 + page * 10,
                                        750 + page * 5,
                                        "2023-01-01T00:00:00Z",
                                        "TypeScript",
                                        null
                                ),
                                new GithubRepositoryItemResponse(
                                        (long) (page * 100 + 3),
                                        "repo-" + page + "-3",
                                        "owner/repo-" + page + "-3",
                                        "https://github.com/owner/repo-" + page + "-3",
                                        3000 + page * 10,
                                        1000 + page * 5,
                                        "2023-01-01T00:00:00Z",
                                        "Python",
                                        null
                                )
                        );

                        // Return mock response with multiple items per page
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
        List<GithubRepositoryItemResponse> results = searchService.searchRepositories(request);
        long executionTime = System.currentTimeMillis() - startTime;

        // Then
        logger.info("🚀 PARALLEL EXECUTION TEST RESULTS 🚀");
        logger.info("Execution time: {}ms (expected max: {}ms for parallel execution)",
                   executionTime, expectedMaxExecutionTimeMs);
        logger.info("Max concurrent calls observed: {} (expected: {})",
                   maxConcurrentCalls.get(), maxPages);
        logger.info("Total items returned: {} (expected: {})",
                   results.size(), expectedTotalItems);

        // Calculate sequential vs parallel performance improvement
        final int sequentialExecutionTimeMs = maxPages * delayPerPageMs; 
        final double performanceImprovement = ((double) (sequentialExecutionTimeMs - executionTime) / sequentialExecutionTimeMs) * 100;
        logger.info("Performance improvement: {:.1f}% (parallel vs sequential)", performanceImprovement);
        logger.info("Sequential would take: {}ms vs Parallel actual: {}ms", sequentialExecutionTimeMs, executionTime);

        // Verify results - should contain all items from all pages
        assertThat(results)
                .hasSize(expectedTotalItems)
                .as("Should contain all items from all pages (%d pages × %d items per page = %d total)",
                    maxPages, itemsPerPage, expectedTotalItems);

        // Verify parallel execution - execution time should be close to single page delay, not sum of all delays
        assertThat(executionTime)
                .as("Execution time should indicate parallel execution (close to single page delay, not cumulative)")
                .isLessThan(expectedMaxExecutionTimeMs);

        // Verify massive performance improvement over sequential execution
        final int sequentialTimeThreshold = sequentialExecutionTimeMs - 1000; // Allow 1 second margin
        assertThat(executionTime)
                .as("Execution time should be MUCH less than sequential execution time (%dms)", sequentialExecutionTimeMs)
                .isLessThan(sequentialTimeThreshold);

        // Verify that many pages were being fetched concurrently
        assertThat(maxConcurrentCalls.get())
                .as("Maximum concurrent calls should be very high, indicating massive parallel execution")
                .isGreaterThan(15) // Expect at least 15 out of 20 calls to be concurrent
                .isLessThanOrEqualTo(maxPages);

        // Verify performance improvement is substantial (at least 80% better than sequential)
        assertThat(performanceImprovement)
                .as("Performance improvement should be substantial (at least 80%% better than sequential)")
                .isGreaterThan(80.0);

        // Verify we got items from different pages (check first, middle, and last page items)
        List<String> repoNames = results.stream()
                .map(GithubRepositoryItemResponse::name)
                .toList();
        assertThat(repoNames)
                .as("Should contain items from all pages")
                .hasSize(expectedTotalItems)
                .contains("repo-1-1", "repo-1-2", "repo-1-3") // First page
                .contains("repo-10-1", "repo-10-2", "repo-10-3") // Middle page
                .contains("repo-20-1", "repo-20-2", "repo-20-3"); // Last page

        // Verify different programming languages are represented (from our mock data)
        List<String> languages = results.stream()
                .map(GithubRepositoryItemResponse::language)
                .distinct()
                .toList();
        assertThat(languages)
                .as("Should contain different programming languages from mock data")
                .contains("Java", "TypeScript", "Python");
    }
}
