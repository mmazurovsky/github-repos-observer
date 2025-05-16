package com.mmazurovsky.redcarecase.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;


import java.time.OffsetDateTime;

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
        String updatedAt,
        @JsonProperty("language")
        @Nullable String language,
        @JsonProperty("created_at")
        @Nullable OffsetDateTime created
) {}