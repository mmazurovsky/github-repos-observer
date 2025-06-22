package com.mmazurovsky.githubreposobserver.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mmazurovsky.githubreposobserver.dto.GithubRepositorySearchResults;
import com.mmazurovsky.githubreposobserver.dto.dto.RepositoryScorePair;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;

@Service
public class ScoringServiceImpl implements ScoringService {
    private static final double STARS_WEIGHT = 0.5;
    private static final double FORKS_WEIGHT = 1.0;
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("#.#");
    public static final int SCORE_RANGE_MIN = 0;
    public static final int SCORE_RANGE_MAX = 10;

    @Override
    public List<RepositoriesSearchOut> convertAndEnrichWithScoreMany(GithubRepositorySearchResults searchResults) {
        List<GithubRepositoryItemResponse> repositories = searchResults.repositories();

        if (repositories.isEmpty()) {
            return List.of();
        }

        // Calculate raw weighted scores for all repositories and create pairs
        List<RepositoryScorePair> rawScoredPairs = repositories.stream()
            .map(repo -> new RepositoryScorePair(repo, calculateRawScoreBasedOnStarsAndForks(repo, searchResults)))
            .toList();

        // Find min and max raw scores for final normalization
        double minRawScore = rawScoredPairs.stream()
            .mapToDouble(RepositoryScorePair::score)
            .min()
            .orElse(0.0);
        double maxRawScore = rawScoredPairs.stream()
            .mapToDouble(RepositoryScorePair::score)
            .max()
            .orElse(10.0);

        // Normalize scores and sort by score descending
        return rawScoredPairs.stream()
            .map(pair -> {
                double normalizedScore = normalizeToRange(
                    pair.score(),
                    minRawScore,
                    maxRawScore,
                    SCORE_RANGE_MIN,
                    SCORE_RANGE_MAX
                );
                return new RepositoryScorePair(pair.repository(), normalizedScore);
            })
            .sorted(Comparator.comparing(RepositoryScorePair::score).reversed())
            .map(pair -> {
                String formattedScore = formatScore(pair.score());
                return convertToRepositoriesSearchOut(pair.repository(), formattedScore);
            })
            .toList();
    }

    private double calculateRawScoreBasedOnStarsAndForks(GithubRepositoryItemResponse repo, GithubRepositorySearchResults searchResults) {
        // Normalize stars to 0-10 range
        double normalizedStars = normalizeToRange(
            repo.stargazersCount(),
            searchResults.minStars(),
            searchResults.maxStars(),
            SCORE_RANGE_MIN,
            SCORE_RANGE_MAX
        );

        // Normalize forks to 0-10 range
        double normalizedForks = normalizeToRange(
            repo.forksCount(),
            searchResults.minForks(),
            searchResults.maxForks(),
            SCORE_RANGE_MIN,
            SCORE_RANGE_MAX
        );

        // Apply weights and return raw score
        return (normalizedStars * STARS_WEIGHT) + (normalizedForks * FORKS_WEIGHT);
    }

    private double normalizeToRange(double value, double minValue, double maxValue, double rangeMin, double rangeMax) {
        if (maxValue == minValue) {
            return rangeMax; // If all values are the same, return max score
        }

        // Linear normalization formula: maps value from [minValue, maxValue] to [newMin, newMax]
        // Step 1: (value - minValue) / (maxValue - minValue) -> converts to 0-1 scale (percentage position)
        // Step 2: * (newMax - newMin) -> scales to new range size
        // Step 3: + newMin -> shifts to start at new minimum
        double normalizedValue = ((value - minValue) / (maxValue - minValue)) * (rangeMax - rangeMin) + rangeMin;
        return Math.max(rangeMin, Math.min(rangeMax, normalizedValue)); // Clamp to range
    }

    private String formatScore(double score) {
        // Round to 1 decimal place
        double rounded = Math.round(score * 10.0) / 10.0;

        // If it's a whole number, return without decimal
        if (rounded == Math.floor(rounded)) {
            return String.valueOf((int) rounded);
        } else {
            return SCORE_FORMAT.format(rounded);
        }
    }

        private RepositoriesSearchOut convertToRepositoriesSearchOut(GithubRepositoryItemResponse item, String popularityScore) {
        String recency = calculateRecency(item.created() != null ? item.created().toLocalDate() : null);

        return new RepositoriesSearchOut(
            item.name(),
            item.htmlUrl(),
            item.language(),
            item.created() != null ? item.created().toLocalDate() : null,
            item.stargazersCount(),
            item.forksCount(),
            recency,
            popularityScore
        );
    }

    private String calculateRecency(LocalDate createdDate) {
        if (createdDate == null) {
            return "Unknown";
        }

        LocalDate now = LocalDate.now();
        Period period = Period.between(createdDate, now);

        if (period.getYears() > 0) {
            return period.getYears() + " year" + (period.getYears() > 1 ? "s" : "") + " ago";
        } else if (period.getMonths() > 0) {
            return period.getMonths() + " month" + (period.getMonths() > 1 ? "s" : "") + " ago";
        } else if (period.getDays() > 0) {
            return period.getDays() + " day" + (period.getDays() > 1 ? "s" : "") + " ago";
        } else {
            return "Today";
        }
    }
}
