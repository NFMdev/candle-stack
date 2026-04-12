package com.github.nfmdev.candlestack.generator_service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.nfmdev.candlestack.generator_service.api.dto.CreateDeliveryRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.CreateScenarioRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.CreateSymbolRequest;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioResponse;
import com.github.nfmdev.candlestack.generator_service.api.dto.ScenarioStatusResponse;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryMode;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.infrastructure.repository.InMemoryScenarioRepository;
import com.github.nfmdev.candlestack.generator_service.runtime.SimulationLifecycleManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

class ScenarioApplicationServiceTest {
    private InMemoryScenarioRepository repository;
    private SimulationLifecycleManager lifecycleManager;
    private ScenarioApplicationService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryScenarioRepository();
        lifecycleManager = mock(SimulationLifecycleManager.class);
        service = new ScenarioApplicationService(repository, lifecycleManager);
    }

    @Test
    void createShouldPersistScenarioAsStopped() {
        ScenarioResponse response = service.create(request());

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("trade-dev");
        assertThat(response.status()).isEqualTo("STOPPED");
        assertThat(response.symbols()).hasSize(1);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void startShouldReturnRunningScenario() {
        ScenarioResponse created = service.create(request());
        UUID scenarioId = created.id();

        SimulationRuntimeState runtimeState = new SimulationRuntimeState(scenarioId, Instant.now());
        runtimeState.initializeSymbol("BTC-USD", new BigDecimal("68000.00"));

        doAnswer(invocation -> {
            SimulationScenario scenario = invocation.getArgument(0);
            scenario.start();
            return runtimeState;
        }).when(lifecycleManager).start(any(SimulationScenario.class));

        when(lifecycleManager.runtimeState(scenarioId)).thenReturn(Optional.of(runtimeState));

        ScenarioStatusResponse response = service.start(scenarioId);

        assertThat(response.id()).isEqualTo(scenarioId);
        assertThat(response.status()).isEqualTo("RUNNING");
        assertThat(response.currentPrices()).containsEntry("BTC-USD", new BigDecimal("68000.00"));
    }

    @Test
    void stausShouldReturnStoppedSnapshotWhenNoRuntimeExists() {
        ScenarioResponse created = service.create(request());
        UUID scenarioId = created.id();

        when(lifecycleManager.runtimeState(scenarioId)).thenReturn(Optional.empty());

        ScenarioStatusResponse response = service.status(scenarioId);

        assertThat(response.status()).isEqualTo("STOPPED");
        assertThat(response.emittedEvents()).isZero();
        assertThat(response.currentSequences()).containsEntry("BTC-USD", 0L);
    }

    private CreateScenarioRequest request() {
        return new CreateScenarioRequest(
                "trade-dev",
                42L,
                List.of(
                    new CreateSymbolRequest(
                        "BTC-USD",
                        new BigDecimal("68000.00"),
                        new BigDecimal("50000.00"),
                        new BigDecimal("90000.00"),
                        new BigDecimal("0.010000"),
                        new BigDecimal("1.500000"),
                        5,
                        "USD"
                    )
                ),
                new CreateDeliveryRequest(
                    DeliveryMode.HTTP,
                    "http://localhost:8081",
                    "/api/v1/market-events",
                    1000,
                    2000
                )
        );
    }



}
