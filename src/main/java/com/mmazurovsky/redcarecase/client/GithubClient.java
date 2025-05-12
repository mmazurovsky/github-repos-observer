package com.mmazurovsky.redcarecase.client;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import reactor.core.publisher.Mono;

public interface GithubClient {
    Mono<GithubRepositorySearchResponse> searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage
    );
}