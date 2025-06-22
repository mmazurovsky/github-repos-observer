package com.mmazurovsky.githubreposobserver.dto.dto;

import com.mmazurovsky.githubreposobserver.dto.external.GithubRepositoryItemResponse;

public record RepositoryScorePair(GithubRepositoryItemResponse repository, double score) {}
