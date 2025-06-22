package com.mmazurovsky.githubreposobserver.dto.dto;

import java.util.List;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;

public record GithubRepositorySearchResults(
    List<GithubRepositoryItemResponse> items,
    int minStars,
    int maxStars,
    int minForks,
    int maxForks
) {}
