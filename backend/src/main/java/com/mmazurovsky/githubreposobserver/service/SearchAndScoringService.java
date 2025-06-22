package com.mmazurovsky.githubreposobserver.service;

import java.util.List;

import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;

public interface SearchAndScoringService {
    List<RepositoriesSearchOut> searchAndOutputRepositoriesWithScores(RepositoriesSearchIn request);
}
