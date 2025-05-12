package com.mmazurovsky.redcarecase.service;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import reactor.core.publisher.Flux;

public interface SearchService {
    Flux<GithubRepositoryItemResponse> searchRepositories(RepositoriesSearchIn request);
}
