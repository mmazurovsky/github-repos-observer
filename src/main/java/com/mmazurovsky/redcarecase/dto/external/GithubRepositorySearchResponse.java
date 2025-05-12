package com.mmazurovsky.redcarecase.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GithubRepositorySearchResponse(
        @JsonProperty("total_count")
        int totalCount,

        @JsonProperty("incomplete_results")
        boolean incompleteResults,

        @JsonProperty("items")
        List<GithubRepositoryItemResponse> items
) {
}
