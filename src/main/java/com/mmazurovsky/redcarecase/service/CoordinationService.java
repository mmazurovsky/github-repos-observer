package com.mmazurovsky.redcarecase.service;

import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.redcarecase.dto.out.RepositoriesSearchOut;
import reactor.core.publisher.Flux;

public interface CoordinationService {
    Flux<RepositoriesSearchOut> searchAndOutputRepositoriesWithScores(RepositoriesSearchIn request);
}
