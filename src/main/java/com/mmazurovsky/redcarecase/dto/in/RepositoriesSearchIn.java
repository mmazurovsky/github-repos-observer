package com.mmazurovsky.redcarecase.dto.in;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record RepositoriesSearchIn(
        @Size(max = 50, message = "Search keywords must be 50 characters or less")
        String keywords,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Past(message = "Earliest created date must be in the past")
        LocalDate earliestCreatedDate,

        @Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "Programming language must be a single string without spaces or commas"
        )
        String language
) {}
