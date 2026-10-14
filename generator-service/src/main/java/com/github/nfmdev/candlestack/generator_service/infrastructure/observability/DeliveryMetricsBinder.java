package com.github.nfmdev.candlestack.generator_service.infrastructure.observability;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.generator_service.runtime.delivery.StripedDeliveryDispatcher;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Component
public final class DeliveryMetricsBinder implements MeterBinder {

    private final StripedDeliveryDispatcher dispatcher;

    public DeliveryMetricsBinder(StripedDeliveryDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(
                "generator.delivery.queue.depth",
                dispatcher,
                StripedDeliveryDispatcher::totalQueueDepth
            )
            .description(
                "Total number of delivery tasks currently queued"
            )
            .register(registry);

        Gauge.builder(
                "generator.delivery.active.workers",
                dispatcher,
                StripedDeliveryDispatcher::activeWorkers
            )
            .description(
                "Number of delivery workers currently executing tasks"
            )
            .register(registry);

        Gauge.builder(
                "generator.delivery.outstanding.payload.bytes",
                dispatcher,
                StripedDeliveryDispatcher::outstandingPayloadBytes
            )
            .description(
                "Estimated payload bytes currently queued or being delivered"
            )
            .baseUnit("bytes")
            .register(registry);

        for (int lane = 0; lane < dispatcher.lanes(); lane++) {
            int laneIndex = lane;

            Gauge.builder(
                    "generator.delivery.lane.queue.depth",
                    () -> dispatcher.queueDepth(laneIndex)
                )
                .description(
                    "Number of queued delivery tasks in a delivery lane"
                )
                .tag(
                    "lane",
                    Integer.toString(laneIndex)
                )
                .register(registry);
        }
    }
}