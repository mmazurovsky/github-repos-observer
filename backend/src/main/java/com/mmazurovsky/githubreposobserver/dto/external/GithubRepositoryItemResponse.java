package com.mmazurovsky.githubreposobserver.dto.external;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepositoryItemResponse(
        long id,
        String name,
        @JsonProperty("full_name")
        String fullName,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("stargazers_count")
        int stargazersCount,
        @JsonProperty("forks_count")
        int forksCount,
        @JsonProperty("updated_at")
        @Nullable String updatedAt,
        @JsonProperty("language")
        @Nullable String language,
        @JsonProperty("created_at")
        @Nullable OffsetDateTime created
) {}
