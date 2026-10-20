package com.github.nfmdev.candlestack.generator_service.domain.engine;

public final class RateController {
    private static final long MAX_EVENTS_PER_TICK = 10_000L;

    private final long eventsPerSecond;
    
    private long startNanos;
    private long generatedEvents;

    public RateController(long eventsPerSecond) {
        if (eventsPerSecond <= 0) {
            throw new IllegalArgumentException("eventsPerSecond must be greater than zero");
        }

        this.eventsPerSecond = eventsPerSecond;
    }
    
    public void start() {
        startNanos = System.nanoTime();
        generatedEvents = 0L;
    }

    public long eventsDue() {
        long elapsedNanos = System.nanoTime() - startNanos;
        
        long expectedEvents = (long) ((elapsedNanos / 1_000_000_000.0) * eventsPerSecond);

        var due = expectedEvents - generatedEvents;

        return Math.min(Math.max(0L, due), MAX_EVENTS_PER_TICK);
    }

    public void recordGenerated(long count) {
        generatedEvents += count;
    } 
}
