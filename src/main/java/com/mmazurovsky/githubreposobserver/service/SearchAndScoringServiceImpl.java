package com.mmazurovsky.githubreposobserver.service;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SearchAndScoringServiceImpl implements SearchAndScoringService {
    private final SearchService searchService;
    private final ScoringService scoringService;

    public SearchAndScoringServiceImpl(SearchService searchService, ScoringService scoringService) {
        this.searchService = searchService;
        this.scoringService = scoringService;
    }

    @Override
    public Flux<RepositoriesSearchOut> searchAndOutputRepositoriesWithScores(RepositoriesSearchIn request) {
        final Flux<GithubRepositoryItemResponse> found = searchService.searchRepositories(request);
        return scoringService.convertAndEnrichWithScoreMany(found);
    }
}
