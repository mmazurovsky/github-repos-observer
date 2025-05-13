package com.mmazurovsky.redcarecase.dto.in;

import com.mmazurovsky.redcarecase.util.Const;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Optional;

public record RepositoriesSearchIn(
        @Size(min = 1, max = 50, message = Const.MSG_KEYWORDS_LENGTH)
        @NotBlank(message = "Keywords must not be blank")
        String keywords,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<@Past(message = Const.MSG_EARLIEST_DATE_PAST) LocalDate> earliestCreatedDate,

        Optional<
                @Size(min = 1, message = Const.MSG_LANGUAGE_LENGTH)
                @Pattern(
                regexp = Const.REGEX_LANGUAGE,
                message = Const.MSG_LANGUAGE_PATTERN
        ) String> language,

        Optional<@Max(value = 10, message = Const.MSG_MAX_PAGES) Integer> maxPages
) {
}
