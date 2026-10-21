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
                "generator.delivery.worker.capacity",
                dispatcher,
                StripedDeliveryDispatcher::workerCapacity
            )
            .description(
                "Maximum number of delivery workers"
            )
            .register(registry);

        Gauge.builder(
                "generator.delivery.queue.capacity",
                dispatcher,
                StripedDeliveryDispatcher::totalQueueCapacity
            )
            .description(
                "Maximum number of delivery tasks across all lane queues"
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

        Gauge.builder(
                "generator.delivery.outstanding.payload.capacity",
                dispatcher,
                StripedDeliveryDispatcher::maxOutstandingPayloadBytes
            )
            .description(
                "Maximum permitted outstanding payload bytes"
            )
            .baseUnit("bytes")
            .register(registry);

        for (int lane = 0; lane < dispatcher.lanes(); lane++) {
            int laneIndex = lane;
            String laneTag = Integer.toString(laneIndex);

            Gauge.builder(
                    "generator.delivery.lane.queue.depth",
                    dispatcher,
                    d -> d.queueDepth(laneIndex)
                )
                .description("Number of queued delivery tasks in a lane")
                .tag("lane", laneTag)
                .register(registry);

            Gauge.builder(
                    "generator.delivery.lane.queue.capacity",
                    dispatcher,
                    d -> d.queueCapacity(laneIndex)
                )
                .description("Maximum number of queued tasks in a lane")
                .tag("lane", laneTag)
                .register(registry);

            Gauge.builder(
                    "generator.delivery.lane.active.workers",
                    dispatcher,
                    d -> d.activeWorkers(laneIndex)
                )
                .description("Number of active delivery workers in a lane")
                .tag("lane", laneTag)
                .register(registry);
        }
    }
}
