package com.mmazurovsky.redcarecase.controller;

import com.mmazurovsky.redcarecase.dto.in.RepositoriesSearchIn;
import com.mmazurovsky.redcarecase.dto.out.RepositoriesSearchOut;
import com.mmazurovsky.redcarecase.service.CoordinationService;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RepositoriesSearchController {

    private final CoordinationService coordinationService;

    public RepositoriesSearchController(CoordinationService coordinationService) {
        this.coordinationService = coordinationService;
    }

    @GetMapping("/search")
    public Mono<List<RepositoriesSearchOut>> searchRepositories(
            @Validated @ModelAttribute RepositoriesSearchIn request
    ) {
        return coordinationService.searchAndOutputRepositoriesWithScores(request).collectList();
    }
}