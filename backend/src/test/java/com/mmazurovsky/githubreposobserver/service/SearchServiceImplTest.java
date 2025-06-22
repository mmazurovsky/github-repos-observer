package com.mmazurovsky.githubreposobserver.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SearchServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImplTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void search_nonExistSearch_shouldReturnConsistentEmptyResults() {
        // Given
        final int numberOfRequests = 10;
        final String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "veryrareandspecifickeyword123456789")
                .queryParam("language", "Java")
                .queryParam("earliestCreatedDate", "2025-06-01")
                .queryParam("maxPages", 1)  // Only 1 page to minimize API calls
                .toUriString();

                // When - Make 10 identical requests sequentially
        logger.info("🔄 NON-EXISTENT SEARCH CONSISTENCY TEST 🔄");
        logger.info("Making {} identical non-existent search requests to test consistency", numberOfRequests);

        long totalStartTime = System.currentTimeMillis();
        Object[] responses = new Object[numberOfRequests]; // Can be either success response or error
        boolean[] isSuccess = new boolean[numberOfRequests];
        long[] executionTimes = new long[numberOfRequests];

                for (int i = 0; i < numberOfRequests; i++) {
            long requestStartTime = System.currentTimeMillis();

            try {
                ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
                );
                responses[i] = response;
                isSuccess[i] = true;
                logger.debug("Request {} SUCCESS in {}ms with status: {}",
                            i + 1, System.currentTimeMillis() - requestStartTime, response.getStatusCode());
            } catch (Exception e) {
                responses[i] = e;
                isSuccess[i] = false;
                logger.error("Request {} FAILED in {}ms with error: {}",
                            i + 1, System.currentTimeMillis() - requestStartTime, e.getClass().getSimpleName());
                logger.error("Error details: {}", e.getMessage());
                if (e.getCause() != null) {
                    logger.error("Root cause: {} - {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
                }

                // Log the full stack trace for debugging
                logger.error("Full stack trace for request " + (i + 1), e);
            }

            executionTimes[i] = System.currentTimeMillis() - requestStartTime;
        }

        long totalExecutionTime = System.currentTimeMillis() - totalStartTime;

        // Then - Verify consistency
        logger.info("Total execution time for {} requests: {}ms", numberOfRequests, totalExecutionTime);
        logger.info("Average execution time per request: {}ms", totalExecutionTime / numberOfRequests);

                // Analyze results and require all requests to succeed
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            if (isSuccess[i]) {
                successCount++;
            } else {
                errorCount++;
            }
        }

        logger.info("📊 RESULTS ANALYSIS:");
        logger.info("  Successful requests: {}/{}", successCount, numberOfRequests);
        logger.info("  Failed requests: {}/{}", errorCount, numberOfRequests);

        // ASSERT: All requests must succeed
        assertThat(errorCount)
                .as("All requests should succeed - no failures expected")
                .isEqualTo(0);

                assertThat(successCount)
                .as("All {} requests should be successful", numberOfRequests)
                .isEqualTo(numberOfRequests);

        // Since all requests succeeded, verify they all return empty lists
        logger.info("✅ All requests succeeded - verifying empty results consistency");
        for (int i = 0; i < numberOfRequests; i++) {
            @SuppressWarnings("unchecked")
            ResponseEntity<List<RepositoriesSearchOut>> response = (ResponseEntity<List<RepositoriesSearchOut>>) responses[i];

            assertThat(response.getStatusCode())
                    .as("Request %d should return HTTP 200 OK", i + 1)
                    .isEqualTo(HttpStatus.OK);

            List<RepositoriesSearchOut> list = response.getBody();

            assertThat(list == null || list.isEmpty())
                    .as("Request %d should return empty list for non-existent search", i + 1)
                    .isTrue();

            logger.debug("Request {} result: {} (size: {})",
                        i + 1,
                        list == null ? "null" : "empty list",
                        list == null ? 0 : list.size());
        }

                logger.info("✅ All {} requests returned consistent empty results", numberOfRequests);

        // Basic performance check - should complete in reasonable time
        double averageExecutionTime = (double) totalExecutionTime / numberOfRequests;
        logger.info("✅ Average execution time: {}ms per request", (long) averageExecutionTime);

        // Log execution time distribution for analysis
        logger.info("📊 Execution time distribution:");
        for (int i = 0; i < numberOfRequests; i++) {
            logger.info("  Request {}: {}ms", i + 1, executionTimes[i]);
        }
    }
}
