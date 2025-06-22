package com.mmazurovsky.githubreposobserver.dto;

import java.util.List;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;

public record GithubRepositorySearchResults(
        int minStars,
        int maxStars,
        int minForks,
        int maxForks,
        List<GithubRepositoryItemResponse> repositories
) {
}
