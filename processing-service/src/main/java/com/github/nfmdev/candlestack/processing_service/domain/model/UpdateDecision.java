package com.github.nfmdev.candlestack.processing_service.domain.model;

public enum UpdateDecision {
    CREATED(true),
    UPDATED(true),
    ROLLED_OVER(true),
    IGNORED_DUPLICATE(false),
    IGNORED_LATE_EVENT(false),
    IGNORED_OLDER_TRADING_DATE(false);

    private final boolean applied;

    UpdateDecision(boolean applied) {
        this.applied = applied;
    }

    public boolean isApplied() {
        return applied;
    }
}