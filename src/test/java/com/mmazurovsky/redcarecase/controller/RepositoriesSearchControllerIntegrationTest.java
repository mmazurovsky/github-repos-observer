package com.mmazurovsky.redcarecase.controller;

import com.mmazurovsky.redcarecase.dto.out.RepositoriesSearchOut;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;


import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoriesSearchControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(RepositoriesSearchControllerValidationTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @Order(1)
    void search_withKeywordsOnly_shouldReturnValidResults() {
        webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("keywords", "bot")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoriesSearchOut.class)
                .value(list -> {
                    logger.debug("Results size: {}", list.size());
                    assertThat(list).isNotEmpty();
                    assertThat(list.getFirst().name()).isNotBlank();
                });
    }

    @Test
    @Order(2)
    void search_withAllFields_shouldReturnValidResultsWithLanguageAndCreated() {
        final var lang = "java";
        final var date = LocalDate.parse("2020-01-01");
        webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("keywords", "bot")
                        .queryParam("language", lang)
                        .queryParam("earliestCreatedDate", date.toString())
                        .queryParam("maxPages", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoriesSearchOut.class)
                .value(list -> {
                    logger.debug("Results size: {}", list.size());
                    assertThat(list).isNotEmpty();
                    assertThat(list.getFirst().popularityScore()).isGreaterThan(0.0);
                    list.forEach(item -> {
                        if (item.language().isPresent()) {
                            assertTrue(item.language().get().toLowerCase().contains(lang));
                        }
                        if (item.created().isPresent()) {
                            assertTrue(item.created().get().isAfter(date));
                        }
                    });
                });
    }

    @Test
    @Order(3)
    void search_withFields_resultsShouldBeSortedByPopularityScoreDesc() {
        webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search")
                        .queryParam("keywords", "bot")
                        .queryParam("language", "Java")
                        .queryParam("earliestCreatedDate", "2020-01-01")
                        .queryParam("maxPages", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoriesSearchOut.class)
                .value(list -> {
                    List<Double> scores = list.stream()
                            .map(RepositoriesSearchOut::popularityScore)
                            .toList();

                    List<Double> sorted = new ArrayList<>(scores);
                    sorted.sort(Comparator.reverseOrder());

                    assertThat(scores).isEqualTo(sorted);
                });
    }

}
