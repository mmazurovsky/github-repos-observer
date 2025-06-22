package com.mmazurovsky.githubreposobserver.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mmazurovsky.githubreposobserver.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.githubreposobserver.dto.out.RepositoriesSearchOut;
import com.mmazurovsky.githubreposobserver.service.SearchAndScoringService;

@RestController
@RequestMapping("/api")
public class RepositoriesSearchController {

    private final SearchAndScoringService searchAndScoringService;

    public RepositoriesSearchController(SearchAndScoringService searchAndScoringService) {
        this.searchAndScoringService = searchAndScoringService;
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RepositoriesSearchOut> searchRepositories(
            @Validated @ModelAttribute RepositoriesSearchIn request
    ) {
        return searchAndScoringService.searchAndOutputRepositoriesWithScores(request);
    }
}
