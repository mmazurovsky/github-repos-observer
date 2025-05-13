package com.mmazurovsky.redcarecase.util;

public final class Const {

    // --- Validation messages -------------------------------------------------
    public static final String MSG_KEYWORDS_LENGTH =
            "Search keywords must be 1 to 50 characters long";

    public static final String MSG_EARLIEST_DATE_PAST =
            "Earliest created date must be in the past";

    public static final String MSG_LANGUAGE_PATTERN =
            "Programming language must be a single string without spaces or commas";

    public static final String MSG_LANGUAGE_LENGTH =
            "Language must be at least 1 characters long";

    public static final String MSG_MAX_PAGES =
            "Max pages to be searched must be less than or equal to 10";

    // --- Regular expressions -------------------------------------------------
    public static final String REGEX_LANGUAGE = "^[a-zA-Z0-9]+$";

    private Const() {
        /* utility class – prevent instantiation */
    }
}