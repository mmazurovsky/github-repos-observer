package com.mmazurovsky.redcarecase.dto.out;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public record RepositoriesSearchOut(
        String name,
        String url,
        @Nullable String language,
        @Nullable LocalDate created,
        int stars,
        int forks,
        String recency,
        double popularityScore
) {
}
