package com.mmazurovsky.githubreposobserver.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import com.mmazurovsky.githubreposobserver.client.GithubClient;
import com.mmazurovsky.githubreposobserver.client.GithubClientImpl;
import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import com.mmazurovsky.githubreposobserver.errorhandling.GlobalErrorHandler.ErrorResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RepositoriesSearchControllerErrorHandlingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private GithubClient githubClient;

    @Test
    void search_whenGitHub422Error_shouldReturnEmptyResultGracefully() {
        // Arrange - Mock GitHub client to return empty response for 422 errors
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(new GithubRepositorySearchResponse(0, false, List.of()));

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        // Assert - Should return 200 OK with empty list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void search_whenGitHubRequestInterrupted_shouldReturnEmptyResultGracefully() {
        // Arrange - Mock GitHub client to return empty response for interrupted requests
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(new GithubRepositorySearchResponse(0, false, List.of()));

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        // Assert - Should return 200 OK with empty list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void search_whenGitHubRetriesExhausted_shouldReturnEmptyResultGracefully() {
        // Arrange - Mock GitHub client to return empty response for exhausted retries
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(new GithubRepositorySearchResponse(0, false, List.of()));

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<List<RepositoriesSearchOut>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RepositoriesSearchOut>>() {}
        );

        // Assert - Should return 200 OK with empty list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void search_whenGitHub4xxClientError_shouldReturn500WithGenericMessage() {
        // Arrange
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(GithubClientImpl.GITHUB_4XX_CLIENT_ERROR_EXCEPTION);

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ErrorResponse.class
        );

        // Assert - Should return generic error message, not internal details
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo(GithubClientImpl.GITHUB_4XX_CLIENT_ERROR_EXCEPTION.getReason());
    }

    @Test
    void search_whenGitHub5xxServerError_shouldReturn500WithGenericMessage() {
        // Arrange
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(GithubClientImpl.GITHUB_5XX_SERVER_ERROR_EXCEPTION);

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ErrorResponse.class
        );

        // Assert - Should return generic error message, not internal details
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo(GithubClientImpl.GITHUB_5XX_SERVER_ERROR_EXCEPTION.getReason());
    }

    @Test
    void search_whenGitHubConnectionError_shouldReturn503WithGenericMessage() {
        // Arrange
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(GithubClientImpl.GITHUB_CONNECTION_ERROR_EXCEPTION);

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ErrorResponse.class
        );

        // Assert - Should return generic error message, not internal details
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo(GithubClientImpl.GITHUB_CONNECTION_ERROR_EXCEPTION.getReason());
    }

    @Test
    void search_whenGitHubClientFailure_shouldReturn500WithGenericMessage() {
        // Arrange
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(GithubClientImpl.GITHUB_CLIENT_FAILURE_EXCEPTION);

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ErrorResponse.class
        );

        // Assert - Should return generic error message, not internal details
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo(GithubClientImpl.GITHUB_CLIENT_FAILURE_EXCEPTION.getReason());
    }

    @Test
    void search_whenUnexpectedRuntimeException_shouldReturn500WithGenericMessage() {
        // Arrange
        when(githubClient.searchRepositories(any(), anyInt(), anyInt(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Internal runtime error"));

        String url = UriComponentsBuilder.fromPath("/api/search")
                .queryParam("keywords", "test")
                .toUriString();

        // Act
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ErrorResponse.class
        );

        // Assert - Should return generic error message from GlobalErrorHandler, not the internal exception message
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Unexpected error");
    }
}
