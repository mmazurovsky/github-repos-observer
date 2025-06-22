package com.mmazurovsky.githubreposobserver.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;

@Service
public class ScoringServiceImpl implements ScoringService {

    @Override
    public List<RepositoriesSearchOut> convertAndEnrichWithScoreMany(List<GithubRepositoryItemResponse> repositoryItems) {
        return repositoryItems.stream()
                .map(this::convertAndEnrichWithScoreOne)
                .sorted(Comparator.comparingDouble(RepositoriesSearchOut::popularityScore).reversed())
                .toList();
    }

    private RepositoriesSearchOut convertAndEnrichWithScoreOne(GithubRepositoryItemResponse repositoryItem) {
        final String recency = calculateRecency(repositoryItem.updatedAt());

        final int popularityScore = calculatePopularityScore(
                repositoryItem.stargazersCount(),
                repositoryItem.forksCount(),
                repositoryItem.updatedAt()
        );

        final LocalDate created = repositoryItem.created() != null ? repositoryItem.created().toLocalDate() : null;

        return new RepositoriesSearchOut(
                repositoryItem.name(),
                repositoryItem.htmlUrl(),
                repositoryItem.language(),
                created,
                repositoryItem.stargazersCount(),
                repositoryItem.forksCount(),
                recency,
                popularityScore
        );
    }

    private String calculateRecency(String updatedAt) {
        final ZonedDateTime updatedDateTime = ZonedDateTime.parse(updatedAt);
        final LocalDateTime now = LocalDateTime.now();
        final long days = ChronoUnit.DAYS.between(updatedDateTime.toLocalDateTime(), now);
        final long hours = ChronoUnit.HOURS.between(updatedDateTime.toLocalDateTime(), now);

        if (days > 0) {
            return days + (days == 1 ? " day" : " days") + " ago";
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour" : " hours") + " ago";
        } else {
            return "was just updated";
        }
    }

    private int calculatePopularityScore(int stars, int forks, String updatedAt) {
        final ZonedDateTime updatedDateTime = ZonedDateTime.parse(updatedAt);
        final LocalDateTime now = LocalDateTime.now();

        // Calculate recency factor (more recent updates get higher score)
        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedDateTime.toLocalDateTime(), now);
        double recencyFactor = Math.max(0, 1 - (daysSinceUpdate / 365.0)); // Less impact after a year

        // Combine stars and forks with recency
        // Use logarithmic scaling to prevent extreme values
        double starScore = Math.log1p(stars);
        double forkScore = Math.log1p(forks);

        // Weighted calculation
        return (int) Math.round((0.5 * starScore + 0.3 * forkScore + 0.2 * recencyFactor) * 10);
    }
}
