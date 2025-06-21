package com.mmazurovsky.githubreposobserver.service;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;
import reactor.core.publisher.Flux;

public interface SearchService {
    Flux<GithubRepositoryItemResponse> searchRepositories(RepositoriesSearchIn request);
}
