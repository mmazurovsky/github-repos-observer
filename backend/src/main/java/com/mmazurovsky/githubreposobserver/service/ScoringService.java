package com.mmazurovsky.githubreposobserver.service;

import java.util.List;

import com.mmazurovsky.githubreposobserver.dto.GithubRepositorySearchResults;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;

public interface ScoringService {
    List<RepositoriesSearchOut> convertAndEnrichWithScoreMany(GithubRepositorySearchResults searchResults);
}
