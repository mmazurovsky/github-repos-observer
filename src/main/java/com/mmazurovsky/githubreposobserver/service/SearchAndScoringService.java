package com.mmazurovsky.githubreposobserver.service;

import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import reactor.core.publisher.Flux;

public interface SearchAndScoringService {
    Flux<RepositoriesSearchOut> searchAndOutputRepositoriesWithScores(RepositoriesSearchIn request);
}
