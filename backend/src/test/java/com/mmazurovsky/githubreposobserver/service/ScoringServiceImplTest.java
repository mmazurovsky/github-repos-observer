package com.mmazurovsky.githubreposobserver.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mmazurovsky.githubreposobserver.dto.GithubRepositorySearchResults;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;

class ScoringServiceImplTest {

    private ScoringServiceImpl scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new ScoringServiceImpl();
    }

    @Test
    void recencyCalculation_shouldUseUpdatedAtInsteadOfCreatedAt() {
        // Given - Repository created 2 years ago but updated yesterday
        OffsetDateTime createdTwoYearsAgo = OffsetDateTime.now().minusYears(2);
        String updatedYesterday = OffsetDateTime.now().minusDays(1).toString();

        GithubRepositoryItemResponse repo = new GithubRepositoryItemResponse(
                1L,
                "test-repo",
                "owner/test-repo",
                "https://github.com/owner/test-repo",
                100,
                50,
                updatedYesterday, // This should be used for recency
                "Java",
                createdTwoYearsAgo // This should NOT be used for recency
        );

        GithubRepositorySearchResults searchResults = new GithubRepositorySearchResults(
                100, 100, 50, 50, List.of(repo)
        );

        // When
        List<RepositoriesSearchOut> results = scoringService.convertAndEnrichWithScoreMany(searchResults);

        // Then
        assertThat(results).hasSize(1);
        RepositoriesSearchOut result = results.get(0);

        // Should show "1 day ago" (using updated_at) instead of "2 years ago" (using created_at)
        assertThat(result.recency()).isEqualTo("1 day ago");
    }

    @Test
    void recencyCalculation_shouldHandleNullUpdatedAt() {
        // Given - Repository with null updated_at
        OffsetDateTime createdTwoYearsAgo = OffsetDateTime.now().minusYears(2);

        GithubRepositoryItemResponse repo = new GithubRepositoryItemResponse(
                1L,
                "test-repo",
                "owner/test-repo",
                "https://github.com/owner/test-repo",
                100,
                50,
                null, // null updated_at
                "Java",
                createdTwoYearsAgo
        );

        GithubRepositorySearchResults searchResults = new GithubRepositorySearchResults(
                100, 100, 50, 50, List.of(repo)
        );

        // When
        List<RepositoriesSearchOut> results = scoringService.convertAndEnrichWithScoreMany(searchResults);

        // Then
        assertThat(results).hasSize(1);
        RepositoriesSearchOut result = results.get(0);

        // Should show "Unknown" when updated_at is null
        assertThat(result.recency()).isEqualTo("Unknown");
    }

    @Test
    void recencyCalculation_shouldHandleInvalidUpdatedAtFormat() {
        // Given - Repository with invalid updated_at format
        OffsetDateTime createdTwoYearsAgo = OffsetDateTime.now().minusYears(2);

        GithubRepositoryItemResponse repo = new GithubRepositoryItemResponse(
                1L,
                "test-repo",
                "owner/test-repo",
                "https://github.com/owner/test-repo",
                100,
                50,
                "invalid-date-format", // invalid updated_at
                "Java",
                createdTwoYearsAgo
        );

        GithubRepositorySearchResults searchResults = new GithubRepositorySearchResults(
                100, 100, 50, 50, List.of(repo)
        );

        // When
        List<RepositoriesSearchOut> results = scoringService.convertAndEnrichWithScoreMany(searchResults);

        // Then
        assertThat(results).hasSize(1);
        RepositoriesSearchOut result = results.get(0);

        // Should show "Unknown" when updated_at format is invalid
        assertThat(result.recency()).isEqualTo("Unknown");
    }
}
