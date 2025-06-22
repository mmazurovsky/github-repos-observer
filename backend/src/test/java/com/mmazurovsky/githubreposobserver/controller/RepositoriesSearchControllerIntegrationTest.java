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
        assertThat(list.get(0).popularityScore()).isGreaterThan(0);
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
        List<Integer> scores = list.stream()
                .map(RepositoriesSearchOut::popularityScore)
                .toList();

        List<Integer> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.reverseOrder());

        assertThat(scores).isEqualTo(sorted);
    }

    @Test
    @Order(4)
    void search_nonExistSearch_shouldReturnEmptyList() {
        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "SAP Instagram bot")
                .queryParam("language", "Murmansk")
                .queryParam("earliestCreatedDate", "2025-05-12")
                .queryParam("maxPages", 10)
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
}
