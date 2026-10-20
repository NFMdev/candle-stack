package com.github.nfmdev.candlestack.generator_service.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.nfmdev.candlestack.generator_service.api.dto.CreateScenarioRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioStatusResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioSummaryResponse;
import com.github.nfmdev.candlestack.generator_service.api.service.ScenarioApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/scenarios")
public class ScenarioController {

    private final ScenarioApplicationService scenarioApplicationService;

    public ScenarioController(ScenarioApplicationService scenarioApplicationService) {
        this.scenarioApplicationService = scenarioApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScenarioResponse create(@Valid @RequestBody CreateScenarioRequest request) {
        return scenarioApplicationService.create(request);
    }

    @GetMapping
    public List<ScenarioSummaryResponse> list() {
        return scenarioApplicationService.list();
    }

    @GetMapping("/{scenarioId}")
    public ScenarioResponse get(@PathVariable UUID scenarioId) {
        return scenarioApplicationService.get(scenarioId);
    }

    @PostMapping("/{scenarioId}/start")
    public ScenarioStatusResponse start(@PathVariable UUID scenarioId) {
        return scenarioApplicationService.start(scenarioId);
    }

    @PostMapping("/{scenarioId}/stop")
    public ScenarioStatusResponse stop(@PathVariable UUID scenarioId) {
        return scenarioApplicationService.stop(scenarioId);
    }

    @GetMapping("/{scenarioId}/status")
    public ScenarioStatusResponse status(@PathVariable UUID scenarioId) {
        return scenarioApplicationService.status(scenarioId);
    }
}