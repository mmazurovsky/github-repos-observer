package com.mmazurovsky.redcarecase.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RepositoriesSearchOut(
        String name,
        String url,
        int stars,
        int forks,
        String recency,
        double popularityScore
) {}
