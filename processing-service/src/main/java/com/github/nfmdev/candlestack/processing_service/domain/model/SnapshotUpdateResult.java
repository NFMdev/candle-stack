package com.github.nfmdev.candlestack.processing_service.domain.model;

import java.util.Objects;

public record SnapshotUpdateResult(
    UpdateDecision decision,
    InstrumentSnapshot snapshot,
    String reason
) {
    public SnapshotUpdateResult {
        Objects.requireNonNull(decision, "decision cannot be null");
    }

    public boolean applied() {
        return decision.isApplied();
    }
}
