package com.mmazurovsky.githubreposobserver.service;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import reactor.core.publisher.Flux;

import java.util.Comparator;

public interface ScoringService {
    Flux<RepositoriesSearchOut> convertAndEnrichWithScoreMany(Flux<GithubRepositoryItemResponse> repositoryItems);
}
