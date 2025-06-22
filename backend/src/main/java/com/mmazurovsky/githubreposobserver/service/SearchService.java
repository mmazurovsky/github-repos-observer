package com.mmazurovsky.githubreposobserver.service;

import com.mmazurovsky.githubreposobserver.dto.GithubRepositorySearchResults;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

public interface SearchService {
    GithubRepositorySearchResults searchRepositories(RepositoriesSearchIn request);
}
