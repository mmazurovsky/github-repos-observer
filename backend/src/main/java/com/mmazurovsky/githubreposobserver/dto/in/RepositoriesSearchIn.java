package com.mmazurovsky.githubreposobserver.dto.in;

import java.time.LocalDate;

import org.jetbrains.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import com.mmazurovsky.githubreposobserver.util.Const;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RepositoriesSearchIn(
        @Size(min = 1, max = 50, message = Const.MSG_KEYWORDS_LENGTH)
        @NotBlank(message = "Keywords must not be blank")
        String keywords,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Past(message = Const.MSG_EARLIEST_DATE_PAST)
        @Nullable
        LocalDate earliestCreatedDate,

        @Size(min = 1, message = Const.MSG_LANGUAGE_LENGTH)
        @Pattern(
                regexp = Const.REGEX_LANGUAGE,
                message = Const.MSG_LANGUAGE_PATTERN
        )
        @Nullable
        String language,

        @Max(value = 5, message = Const.MSG_MAX_PAGES)
        @Nullable
        Integer maxPages
) {
}
