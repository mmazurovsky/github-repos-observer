package com.mmazurovsky.githubreposobserver.service;

import java.util.List;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

public interface SearchService {
    List<GithubRepositoryItemResponse> searchRepositories(RepositoriesSearchIn request);
}
