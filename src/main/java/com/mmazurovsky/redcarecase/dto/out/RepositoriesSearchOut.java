package com.mmazurovsky.redcarecase.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Optional;

public record RepositoriesSearchOut(
        String name,
        String url,
        Optional<String> language,
        Optional<LocalDate> created,
        int stars,
        int forks,
        String recency,
        double popularityScore
) {
}
