package com.mmazurovsky.githubreposobserver.dto.out;

import java.time.LocalDate;

import org.jetbrains.annotations.Nullable;

public record RepositoriesSearchOut(
        String name,
        String url,
        @Nullable String language,
        @Nullable LocalDate created,
        int stars,
        int forks,
        String recency,
        String popularityScore
) {
}
