package com.mmazurovsky.githubreposobserver.client;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositorySearchResponse;
import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;

public interface GithubClient {
    GithubRepositorySearchResponse searchRepositories(
            RepositoriesSearchIn request,
            int page,
            int perPage
    );
}
