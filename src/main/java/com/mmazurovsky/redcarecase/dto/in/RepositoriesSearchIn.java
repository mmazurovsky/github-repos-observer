package com.mmazurovsky.redcarecase.dto.in;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Optional;

public record RepositoriesSearchIn(
        @Size(max = 50, min = 1, message = "Search keywords must be 50 characters or less")
        String keywords,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<@Past(message = "Earliest created date must be in the past") LocalDate> earliestCreatedDate,

        Optional<@Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "Programming language must be a single string without spaces or commas"
        ) String> language,

        Optional<@Max(value = 20, message = "Max pages to be searched must be less than or equal to 20") Integer> maxPages
) {
}
