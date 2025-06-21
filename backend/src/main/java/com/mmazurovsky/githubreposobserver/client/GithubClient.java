package com.mmazurovsky.githubreposobserver.client;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;
import reactor.core.publisher.Mono;

public interface GithubClient {
    Mono<GithubRepositorySearchResponse> searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage
    );
}