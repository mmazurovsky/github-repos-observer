package com.mmazurovsky.githubreposobserver.controller;

import java.time.LocalDate;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import com.mmazurovsky.githubreposobserver.service.SearchAndScoringService;
import com.mmazurovsky.githubreposobserver.util.Const;

@WebMvcTest(RepositoriesSearchController.class)
public class RepositoriesSearchControllerValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(RepositoriesSearchControllerValidationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchAndScoringService searchAndScoringService;

    @Test
    void whenKeywordsTooLong_thenValidationFails() throws Exception {
        String longKeyword = "a".repeat(51);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", longKeyword))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value(Matchers.containsString(Const.MSG_KEYWORDS_LENGTH)));
    }

    @Test
    void whenInvalidPastDateProvided_thenValidationFails() throws Exception {
        String futureDate = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", "java")
                        .param("earliestCreatedDate", futureDate))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value(Matchers.containsString(Const.MSG_EARLIEST_DATE_PAST)));
    }

    @Test
    void whenInvalidLanguage_thenValidationFails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", "java")
                        .param("language", "java,python"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value(Matchers.containsString(Const.MSG_LANGUAGE_PATTERN)));
    }

    @Test
    void whenMaxPagesExceeds_thenValidationFails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", "java")
                        .param("maxPages", "25"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value(Matchers.containsString(Const.MSG_MAX_PAGES)));
    }

    @Test
    void whenMaxPagesExceedsAndInvalidLanguage_thenValidationFails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", "java")
                        .param("language", "java,python")
                        .param("maxPages", "25"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value(Matchers.allOf(Matchers.containsString(Const.MSG_MAX_PAGES),
                                Matchers.containsString(Const.MSG_LANGUAGE_PATTERN))));
    }

    @Test
    void whenAllValid_thenPasses() throws Exception {
        // Mock service response
        RepositoriesSearchOut mockResult = new RepositoriesSearchOut("repo", "url", null, null, 1, 1, "recent", "8.5");
        Mockito.when(searchAndScoringService.searchAndOutputRepositoriesWithScores(Mockito.any()))
                .thenReturn(List.of(mockResult));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", "java")
                        .param("earliestCreatedDate", "2022-01-01")
                        .param("language", "Java")
                        .param("maxPages", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)));
    }

    @Test
    void whenOnlyKeywordsProvided_thenPasses() throws Exception {
        // Mock service response
        RepositoriesSearchOut mockResult = new RepositoriesSearchOut("repo", "url", null, null, 1, 1, "recent", "7.2");
        Mockito.when(searchAndScoringService.searchAndOutputRepositoriesWithScores(Mockito.any()))
                .thenReturn(List.of(mockResult));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/search")
                        .param("keywords", "java"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)));
    }
}

