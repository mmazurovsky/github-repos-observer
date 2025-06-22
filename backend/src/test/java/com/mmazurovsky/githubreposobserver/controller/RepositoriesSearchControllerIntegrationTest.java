package com.mmazurovsky.githubreposobserver.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import com.mmazurovsky.githubreposobserver.service.ScoringServiceImpl;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class RepositoriesSearchControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(RepositoriesSearchControllerValidationTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    void search_withKeywordsOnly_shouldReturnValidResults() {
        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "bot")
                .toUriString();

        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RepositoriesSearchOut> list = response.getBody();
        logger.debug("Results size: {}", list != null ? list.size() : 0);
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).name()).isNotBlank();
    }

    @Test
    @Order(2)
    void search_withAllFields_shouldReturnValidResultsWithLanguageAndCreated() {
        final var lang = "java";
        final var date = LocalDate.parse("2020-01-01");

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "bot")
                .queryParam("language", lang)
                .queryParam("earliestCreatedDate", date.toString())
                .queryParam("maxPages", 3)
                .toUriString();

        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RepositoriesSearchOut> list = response.getBody();
        logger.debug("Results size: {}", list != null ? list.size() : 0);
        assertThat(list).isNotEmpty();
        assertThat(Double.parseDouble(list.get(0).popularityScore())).isGreaterThan(0.0);
        list.forEach(item -> {
            if (item.language() != null) {
                assertTrue(item.language().toLowerCase().contains(lang));
            }
            if (item.created() != null) {
                assertTrue(item.created().isAfter(date));
            }
        });
    }

    @Test
    @Order(3)
    void search_withFields_resultsShouldBeSortedByPopularityScoreDesc() {
        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "bot")
                .queryParam("language", "Java")
                .queryParam("earliestCreatedDate", "2020-01-01")
                .queryParam("maxPages", 3)
                .toUriString();

        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RepositoriesSearchOut> list = response.getBody();
        List<Double> scores = list.stream()
                .map(item -> Double.parseDouble(item.popularityScore()))
                .toList();

        List<Double> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.reverseOrder());

        assertThat(scores).isEqualTo(sorted);
    }

    @Test
    @Order(4)
    void search_nonExistSearch_shouldReturnEmptyList() {
        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "veryrareandspecifickeyword123456789")
                .queryParam("language", "Java")
                .queryParam("earliestCreatedDate", "2025-06-01")
                .queryParam("maxPages", 1)  // Only 1 page to minimize API calls
                .toUriString();

        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RepositoriesSearchOut> list = response.getBody();
        assertTrue(list == null || list.isEmpty());
    }

    @Test
    @Order(5)
    void search_popularityScores_shouldBeWithinDefinedBounds() {
        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "spring")
                .queryParam("language", "Java")
                .queryParam("maxPages", 5)
                .toUriString();

        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RepositoriesSearchOut> list = response.getBody();
        assertThat(list).isNotEmpty();

        logger.info("🎯 POPULARITY SCORE BOUNDS TEST 🎯");
        logger.info("Testing {} repositories for score bounds [{}, {}]",
                   list.size(), ScoringServiceImpl.SCORE_RANGE_MIN, ScoringServiceImpl.SCORE_RANGE_MAX);

        // Verify all popularity scores are within bounds
        list.forEach(repo -> {
            double score = Double.parseDouble(repo.popularityScore());
            logger.debug("Repository: {}, Score: {}, Stars: {}, Forks: {}",
                        repo.name(), score, repo.stars(), repo.forks());

            assertThat(score)
                .as("Popularity score for repository '%s' should be within bounds [%d, %d]",
                    repo.name(), ScoringServiceImpl.SCORE_RANGE_MIN, ScoringServiceImpl.SCORE_RANGE_MAX)
                .isGreaterThanOrEqualTo(ScoringServiceImpl.SCORE_RANGE_MIN)
                .isLessThanOrEqualTo(ScoringServiceImpl.SCORE_RANGE_MAX);
        });

        // Additional verification: check that we have some variation in scores
        List<Double> scores = list.stream()
                .map(item -> Double.parseDouble(item.popularityScore()))
                .distinct()
                .toList();

        assertThat(scores.size())
                .as("Should have some variation in popularity scores (not all the same)")
                .isGreaterThan(1);

        // Verify we have the expected minimum and maximum scores in the dataset
        double minScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(-1);
        double maxScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(-1);

        logger.info("Score range in results: [{}, {}]", minScore, maxScore);

        assertThat(minScore)
                .as("Minimum score should be at least the defined minimum")
                .isGreaterThanOrEqualTo(ScoringServiceImpl.SCORE_RANGE_MIN);

        assertThat(maxScore)
                .as("Maximum score should be at most the defined maximum")
                .isLessThanOrEqualTo(ScoringServiceImpl.SCORE_RANGE_MAX);
    }
}
