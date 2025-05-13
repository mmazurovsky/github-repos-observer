package com.mmazurovsky.redcarecase.service;

import com.mmazurovsky.redcarecase.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.redcarecase.dto.out.RepositoriesSearchOut;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
        final Flux<RepositoriesSearchOut> enrichedWithScore = scoringService.convertAndEnrichWithScoreMany(found);
        return enrichedWithScore;
    }

}
