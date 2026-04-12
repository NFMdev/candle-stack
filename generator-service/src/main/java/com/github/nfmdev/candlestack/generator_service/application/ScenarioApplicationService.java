package com.github.nfmdev.candlestack.generator_service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.nfmdev.candlestack.generator_service.api.dto.CreateDeliveryRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.CreateScenarioRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.CreateSymbolRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.DeliveryConfigResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioStatusResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioSummaryResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.SymbolConfigResponse;
import com.github.nfmdev.candlestack.generator_service.domain.exception.ScenarioNotFoundException;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;
import com.github.nfmdev.candlestack.generator_service.domain.ports.ScenarioRepository;
import com.github.nfmdev.candlestack.generator_service.runtime.SimulationLifecycleManager;

@Service
public class ScenarioApplicationService {
    private final ScenarioRepository scenarioRepository;
    private final SimulationLifecycleManager lifeCycleManager;

    public ScenarioApplicationService(
            ScenarioRepository scenarioRepository,
            SimulationLifecycleManager lifeCycleManager) {
        this.scenarioRepository = scenarioRepository;
        this.lifeCycleManager = lifeCycleManager;
    }

    public ScenarioResponse create(CreateScenarioRequest request) {
        Instant now = Instant.now();

        SimulationScenario scenario = new SimulationScenario(
                UUID.randomUUID(),
                request.name(),
                request.symbols().stream().map(this::toDomain).toList(),
                toDomain(request.delivery()),
                request.seed(),
                now,
                now);
        scenarioRepository.save(scenario);
        return toResponse(scenario);
    }

    public List<ScenarioSummaryResponse> list() {
        return scenarioRepository.findAll()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    public ScenarioResponse get(UUID id) {
        return toResponse(findScenario(id));
    }

    public ScenarioStatusResponse start(UUID id) {
        SimulationScenario scenario = findScenario(id);
        lifeCycleManager.start(scenario);
        scenarioRepository.save(scenario);
        return status(id);
    }

    public ScenarioStatusResponse stop(UUID id) {
        SimulationScenario scenario = findScenario(id);
        lifeCycleManager.stop(scenario);
        scenarioRepository.save(scenario);
        return status(id);
    }

    public ScenarioStatusResponse status(UUID id) {
        SimulationScenario scenario = findScenario(id);

        return lifeCycleManager.runtimeState(id)
                .map(runtime -> toStatusResponse(scenario, runtime))
                .orElseGet(() -> stoppedStatusResponse(scenario));
    }

    private SimulationScenario findScenario(UUID id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new ScenarioNotFoundException(id));
    }

    private SymbolConfig toDomain(CreateSymbolRequest request) {
        return new SymbolConfig(
                request.symbol(),
                request.initialPrice(),
                request.minPrice(),
                request.maxPrice(),
                request.minQuantity(),
                request.maxQuantity(),
                request.ticksPerSecond(),
                request.currency());
    }

    private DeliveryConfig toDomain(CreateDeliveryRequest request) {
        return new DeliveryConfig(
                request.mode(),
                request.ingestionBaseUrl(),
                request.endpointPath(),
                request.connectTimeoutMs(),
                request.readTimeoutMs());
    }

    private ScenarioResponse toResponse(SimulationScenario scenario) {
        return new ScenarioResponse(
                scenario.getId(),
                scenario.getName(),
                scenario.getStatus().name(),
                scenario.getSeed(),
                scenario.getSymbols().stream().map(this::toResponse).toList(),
                toResponse(scenario.getDeliveryConfig()),
                scenario.getCreatedAt(),
                scenario.getUpdatedAt());
    }

    private ScenarioSummaryResponse toSummaryResponse(SimulationScenario scenario) {
        return new ScenarioSummaryResponse(
                scenario.getId(),
                scenario.getName(),
                scenario.getStatus().name(),
                scenario.getSymbols().size(),
                scenario.getCreatedAt(),
                scenario.getUpdatedAt());
    }

    private ScenarioStatusResponse toStatusResponse(SimulationScenario scenario, SimulationRuntimeState runtime) {
        return new ScenarioStatusResponse(
                scenario.getId(),
                scenario.getStatus().name(),
                runtime.getStartedAt(),
                runtime.getEmittedEvents(),
                runtime.getDeliveredEvents(),
                runtime.getDeliveryFailures(),
                runtime.currentPricesSnapshot(),
                runtime.currentSequencesSnapshot());
    }

    private ScenarioStatusResponse stoppedStatusResponse(SimulationScenario scenario) {
        Map<String, BigDecimal> prices = scenario.getSymbols().stream()
                .collect(Collectors.toMap(SymbolConfig::symbol, SymbolConfig::initialPrice));

        Map<String, Long> sequences = scenario.getSymbols().stream()
                .collect(Collectors.toMap(SymbolConfig::symbol, ignored -> 0L));

        return new ScenarioStatusResponse(
                scenario.getId(),
                scenario.getStatus().name(),
                null,
                0L,
                0L,
                0L,
                prices,
                sequences);
    }

    private SymbolConfigResponse toResponse(SymbolConfig symbol) {
        return new SymbolConfigResponse(
                symbol.symbol(),
                symbol.initialPrice(),
                symbol.minPrice(),
                symbol.maxPrice(),
                symbol.minQuantity(),
                symbol.maxQuantity(),
                symbol.ticksPerSecond(),
                symbol.currency());
    }

    private DeliveryConfigResponse toResponse(DeliveryConfig delivery) {
        return new DeliveryConfigResponse(
                delivery.mode().name(),
                delivery.ingestionBaseUrl(),
                delivery.endpointPath(),
                delivery.connectTimeoutMs(),
                delivery.readTimeoutMs());
    }
}