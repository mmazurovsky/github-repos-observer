package com.mmazurovsky.githubreposobserver.controller;

import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import com.mmazurovsky.githubreposobserver.service.SearchAndScoringService;
import com.mmazurovsky.githubreposobserver.util.Const;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class RepositoriesSearchControllerValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(RepositoriesSearchControllerValidationTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SearchAndScoringService searchAndScoringService;

    @Test
    void whenKeywordsTooLong_thenValidationFails() {
        String longKeyword = "a".repeat(51);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", longKeyword)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error")
                .value(Matchers.containsString(Const.MSG_KEYWORDS_LENGTH));
    }

    @Test
    void whenInvalidPastDateProvided_thenValidationFails() {
        String futureDate = LocalDate.now().plusDays(1).toString();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", "java")
                        .queryParam("earliestCreatedDate", futureDate)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error")
                .value(Matchers.containsString(Const.MSG_EARLIEST_DATE_PAST));
    }

    @Test
    void whenInvalidLanguage_thenValidationFails() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", "java")
                        .queryParam("language", "java,python")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error")
                .value(Matchers.containsString(Const.MSG_LANGUAGE_PATTERN));
    }

    @Test
    void whenMaxPagesExceeds_thenValidationFails() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", "java")
                        .queryParam("maxPages", 25)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error")
                .value(Matchers.containsString(Const.MSG_MAX_PAGES));
    }

    @Test
    void whenMaxPagesExceedsAndInvalidLanguage_thenValidationFails() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", "java")
                        .queryParam("language", "java,python")
                        .queryParam("maxPages", 25)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(result -> {
                    logger.debug(new String(result.getResponseBody()));
                })
                .jsonPath("$.error")
                .value(Matchers.allOf(Matchers.containsString(Const.MSG_MAX_PAGES),
                        Matchers.containsString(Const.MSG_LANGUAGE_PATTERN)));


    }

    @Test
    void whenAllValid_thenPasses() {
        // Mock service response
        Mockito.when(searchAndScoringService.searchAndOutputRepositoriesWithScores(Mockito.any()))
                .thenReturn(Flux.just(new RepositoriesSearchOut("repo", "url", null, null, 1, 1, "recent", 0.1)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", "java")
                        .queryParam("earliestCreatedDate", "2022-01-01")
                        .queryParam("language", "Java")
                        .queryParam("maxPages", 5)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoriesSearchOut.class)
                .hasSize(1);
    }

    @Test
    void whenOnlyKeywordsProvided_thenPasses() {
        // Mock service response
        Mockito.when(searchAndScoringService.searchAndOutputRepositoriesWithScores(Mockito.any()))
                .thenReturn(Flux.just(new RepositoriesSearchOut("repo", "url", null, null, 1, 1, "recent", 0.1)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/search")
                        .queryParam("keywords", "java")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoriesSearchOut.class)
                .hasSize(1);
    }
}

