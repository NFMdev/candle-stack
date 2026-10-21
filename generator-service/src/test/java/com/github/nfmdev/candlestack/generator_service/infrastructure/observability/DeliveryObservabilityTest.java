package com.github.nfmdev.candlestack.generator_service.infrastructure.observability;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.github.nfmdev.candlestack.generator_service.domain.engine.fault.FaultConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.DeliveryMode;
import com.github.nfmdev.candlestack.generator_service.domain.model.LoadConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationRuntimeState;
import com.github.nfmdev.candlestack.generator_service.domain.model.SimulationScenario;
import com.github.nfmdev.candlestack.generator_service.domain.model.SymbolConfig;
import com.github.nfmdev.candlestack.generator_service.domain.model.TradeSide;
import com.github.nfmdev.candlestack.generator_service.domain.ports.EventDeliveryPort;
import com.github.nfmdev.candlestack.generator_service.infrastructure.config.DeliveryExecutorProperties;
import com.github.nfmdev.candlestack.generator_service.runtime.delivery.StripedDeliveryDispatcher;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

class DeliveryObservabilityTest {

    @Test
    void publishesConfiguredDeliveryCapacities() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        GeneratorMetrics metrics = new GeneratorMetrics(registry);
        StripedDeliveryDispatcher dispatcher = new StripedDeliveryDispatcher(
            (scenario, event) -> { },
            properties(2, 3, 1_024),
            metrics
        );

        try {
            new DeliveryMetricsBinder(dispatcher).bindTo(registry);

            assertThat(gauge(registry, "generator.delivery.worker.capacity")).isEqualTo(2);
            assertThat(gauge(registry, "generator.delivery.queue.capacity")).isEqualTo(6);
            assertThat(gauge(registry, "generator.delivery.outstanding.payload.capacity")).isEqualTo(1_024);
            assertThat(registry.scrape())
                .contains("generator_delivery_worker_capacity")
                .contains("generator_delivery_queue_capacity")
                .contains("generator_delivery_outstanding_payload_capacity_bytes");
        } finally {
            dispatcher.shutdown();
            registry.close();
        }
    }

    @Test
    void totalRejectionCounterIncludesByteBudgetAndShutdownRejections() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        GeneratorMetrics metrics = new GeneratorMetrics(registry);
        StripedDeliveryDispatcher dispatcher = new StripedDeliveryDispatcher(
            (scenario, event) -> { },
            properties(1, 1, 10),
            metrics
        );
        UUID scenarioId = UUID.randomUUID();
        SimulationRuntimeState state = new SimulationRuntimeState(scenarioId, Instant.now());

        try {
            assertThat(dispatcher.dispatch(scenario(scenarioId), event(scenarioId), state, 1, 11)).isFalse();
            dispatcher.shutdown();
            assertThat(dispatcher.dispatch(scenario(scenarioId), event(scenarioId), state, 1, 1)).isFalse();

            assertThat(counter(registry, "generator.delivery.rejected")).isEqualTo(2);
            assertThat(counter(registry, "generator.delivery.byte.budget.rejected")).isEqualTo(1);
            assertThat(state.getRejected()).isEqualTo(2);
        } finally {
            dispatcher.shutdown();
            registry.close();
        }
    }

    @Test
    void totalRejectionCounterIncludesQueueCapacityRejections() throws InterruptedException {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        GeneratorMetrics metrics = new GeneratorMetrics(registry);
        CountDownLatch deliveryStarted = new CountDownLatch(1);
        CountDownLatch releaseDelivery = new CountDownLatch(1);
        EventDeliveryPort deliveryPort = (scenario, event) -> {
            deliveryStarted.countDown();
            try {
                releaseDelivery.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        };
        StripedDeliveryDispatcher dispatcher = new StripedDeliveryDispatcher(
            deliveryPort,
            properties(1, 1, 1_024),
            metrics
        );
        UUID scenarioId = UUID.randomUUID();
        SimulationScenario scenario = scenario(scenarioId);
        MarketTradeEvent event = event(scenarioId);
        SimulationRuntimeState state = new SimulationRuntimeState(scenarioId, Instant.now());

        try {
            assertThat(dispatcher.dispatch(scenario, event, state, 1, 1)).isTrue();
            assertThat(deliveryStarted.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(dispatcher.dispatch(scenario, event, state, 1, 1)).isTrue();
            assertThat(dispatcher.dispatch(scenario, event, state, 1, 1)).isFalse();

            assertThat(counter(registry, "generator.delivery.rejected")).isEqualTo(1);
            assertThat(counter(registry, "generator.delivery.queue.rejected")).isEqualTo(1);
            assertThat(state.getQueueRejected()).isEqualTo(1);
        } finally {
            releaseDelivery.countDown();
            dispatcher.awaitScenarioDrained(scenarioId);
            dispatcher.shutdown();
            registry.close();
        }
    }

    private DeliveryExecutorProperties properties(int lanes, int queueCapacity, long byteCapacity) {
        return new DeliveryExecutorProperties(
            lanes,
            queueCapacity,
            byteCapacity,
            Duration.ofSeconds(2)
        );
    }

    private SimulationScenario scenario(UUID scenarioId) {
        Instant now = Instant.now();
        SymbolConfig symbol = new SymbolConfig(
            "BTC-USD",
            new BigDecimal("68000"),
            new BigDecimal("50000"),
            new BigDecimal("90000"),
            new BigDecimal("0.01"),
            new BigDecimal("1.5"),
            1,
            "USD"
        );
        return new SimulationScenario(
            scenarioId,
            "observability-test",
            List.of(symbol),
            new LoadConfig(1, 0, Duration.ZERO),
            new DeliveryConfig(
                DeliveryMode.HTTP,
                "http://localhost:8081",
                "/api/v1/market-events",
                1_000,
                1_000
            ),
            FaultConfig.none(),
            42L,
            now,
            now
        );
    }

    private MarketTradeEvent event(UUID scenarioId) {
        return new MarketTradeEvent(
            UUID.randomUUID(),
            scenarioId,
            "observability-test",
            "TRADE",
            "BTC-USD",
            Instant.now(),
            1,
            new BigDecimal("68000"),
            new BigDecimal("0.01"),
            TradeSide.BUY,
            "USD",
            null
        );
    }

    private double gauge(MeterRegistry registry, String name) {
        return registry.get(name).gauge().value();
    }

    private double counter(SimpleMeterRegistry registry, String name) {
        return registry.get(name).counter().count();
    }
}
