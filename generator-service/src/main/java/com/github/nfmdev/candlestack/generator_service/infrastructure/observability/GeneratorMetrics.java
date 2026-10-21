package com.github.nfmdev.candlestack.generator_service.infrastructure.observability;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component 
public final class GeneratorMetrics {
    private final Counter emittedEvents;
    private final Counter enqueuedEvents;

    private final Counter rejectedDeliveries;
    private final Counter queueRejectedDeliveries;
    private final Counter byteBudgetRejectedDeliveries;

    private final Counter successfulDeliveries;
    private final Counter failedDeliveries;

    private final Counter duplicateEvents;
    private final Counter delayedEvents;

    private final Timer deliveryDuration;
    private final Timer queueWaitDuration;

    public GeneratorMetrics(MeterRegistry registry) {
        this.emittedEvents = Counter
            .builder("generator.events.emitted")
            .description("Logical market events generated")
            .register(registry);

        this.enqueuedEvents = Counter
            .builder("generator.events.enqueued")
            .description("Delivery tasks accepted by the dispatcher")
            .register(registry);

        this.rejectedDeliveries = Counter
            .builder("generator.delivery.rejected")
            .description("Delivery tasks rejected before execution")
            .register(registry);

        this.queueRejectedDeliveries = Counter
            .builder("generator.delivery.queue.rejected")
            .description("Delivery tasks rejected because a lane queue was full")
            .register(registry);

        this.byteBudgetRejectedDeliveries = Counter
            .builder("generator.delivery.byte.budget.rejected")
            .description("Delivery tasks rejected because the outstanding payload byte budget was exhausted")
            .register(registry);

        this.successfulDeliveries = Counter
            .builder("generator.delivery.success")
            .description("Successful HTTP event delivery attempts")
            .register(registry);

        this.failedDeliveries = Counter
            .builder("generator.delivery.failure")
            .description("Failed HTTP event delivery attempts")
            .register(registry);

        this.duplicateEvents = Counter
            .builder("generator.duplicate.events")
            .description("Logical events selected for duplicate delivery")
            .register(registry);

        this.delayedEvents = Counter
            .builder("generator.delayed.events")
            .description("Logical events generated with an intentionally late timestamp")
            .register(registry);

        this.deliveryDuration = Timer.builder("generator.delivery.duration")
            .description("Duration of individual HTTP delivery attempts")
            .publishPercentileHistogram()
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(10))
            .register(registry);

        this.queueWaitDuration = Timer.builder("generator.delivery.queue.wait")
            .description("Time spent waiting for a delivery lane worker")
            .publishPercentileHistogram()
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(10))
            .register(registry);
    }

    public void eventEmitted() {
        emittedEvents.increment();
    }

    public void eventEnqueued() {
        enqueuedEvents.increment();
    }

    public void deliveryRejected() {
        rejectedDeliveries.increment();
    }

    public void queueRejected() {
        queueRejectedDeliveries.increment();
    }

    public void byteBudgetRejected() {
        byteBudgetRejectedDeliveries.increment();
    }

    public void deliverySucceeded() {
        successfulDeliveries.increment();
    }

    public void deliveryFailed() {
        failedDeliveries.increment();
    }

    public void duplicateEvent() {
        duplicateEvents.increment();
    }

    public void delayedEvent() {
        delayedEvents.increment();
    }

    public void recordDeliveryDuration(long durationNanos) {
        deliveryDuration.record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void recordQueueWaitDuration(long durationNanos) {
        queueWaitDuration.record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
