package com.github.nfmdev.candlestack.generator_service.runtime.delivery;

final class ScenarioDeliveryTracker {
    private long pending;

    public synchronized void increment() {
        pending++;
    }

    public synchronized void decrement() {
        if (pending <= 0) {
            throw new IllegalStateException(
                "Pending delivery count cannot be negative"
            );
        }

        pending--;

        if (pending == 0) notifyAll();
    }

    public synchronized long pending() {
        return pending;
    }

    public synchronized void awaitEmpty() throws InterruptedException {
        while (pending > 0) {
            wait();
        }
    }
}
