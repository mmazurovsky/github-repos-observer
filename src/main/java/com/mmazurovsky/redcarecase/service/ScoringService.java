package com.mmazurovsky.redcarecase.service;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.redcarecase.dto.out.RepositoriesSearchOut;
import reactor.core.publisher.Flux;

public interface ScoringService {
    Flux<RepositoriesSearchOut> convertAndEnrichWithScoreMany(Flux<GithubRepositoryItemResponse> repositoryItems);
}
