package com.github.nfmdev.candlestack.processing_service.domain.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.domain.model.SnapshotUpdateResult;
import com.github.nfmdev.candlestack.processing_service.domain.model.UpdateDecision;

public class InstrumentSnapshotCalculator {
    private final Clock clock;

    public InstrumentSnapshotCalculator() {
        this(Clock.systemUTC());
    }

    public InstrumentSnapshotCalculator(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    public SnapshotUpdateResult apply(InstrumentSnapshot currentSnapshot, TradeEvent tradeEvent) {
        Objects.requireNonNull(tradeEvent, "tradeEvent cannot be null");

        LocalDate tradeDate = tradingDateOf(tradeEvent.eventTime());

        if (currentSnapshot == null) {
            InstrumentSnapshot created = createSnapshot(tradeEvent, tradeDate);
            return new SnapshotUpdateResult(UpdateDecision.CREATED, created, null);
        }

        ensureSameInstrument(currentSnapshot, tradeEvent);

        if (currentSnapshot.lastEventId().equals((tradeEvent.eventId()))) {
            return new SnapshotUpdateResult(
                UpdateDecision.IGNORED_DUPLICATE,
                currentSnapshot,
                "Duplicate eventId: " + tradeEvent.eventId()
            );
        }

        if (tradeDate.isBefore(currentSnapshot.tradingDate())) {
            return new SnapshotUpdateResult(
                UpdateDecision.IGNORED_OLDER_TRADING_DATE,
                currentSnapshot,
                "Trade tradingDate is older than current snapshot tradingDate"
            );
        }

        if (tradeEvent.eventTime().isBefore(currentSnapshot.lastEventTime())) {
            return new SnapshotUpdateResult(
                UpdateDecision.IGNORED_LATE_EVENT,
                currentSnapshot,
                "Trade eventTime is older than snapshot lastEventTime"
            );
        }

        if (tradeDate.isAfter(currentSnapshot.tradingDate())) {
            InstrumentSnapshot rolledOver = createSnapshot(tradeEvent, tradeDate);
            return new SnapshotUpdateResult(UpdateDecision.ROLLED_OVER, rolledOver, null);
        }

        InstrumentSnapshot updated = new InstrumentSnapshot(
            currentSnapshot.instrumentId(),
            tradeEvent.eventId(),
            currentSnapshot.tradingDate(),
            tradeEvent.price(),
            tradeEvent.quantity(),
            tradeEvent.eventTime(),
            currentSnapshot.dayVolume().add(tradeEvent.quantity()),
            currentSnapshot.dayHigh().max(tradeEvent.price()),
            currentSnapshot.dayLow().min(tradeEvent.price()),
            currentSnapshot.tradeCount()+1,
            Instant.now(clock),
            currentSnapshot.currency()
        );

        return new SnapshotUpdateResult(UpdateDecision.UPDATED, updated, null);
    }

    private InstrumentSnapshot createSnapshot(TradeEvent tradeEvent, LocalDate tradeDate) {
        return new InstrumentSnapshot(
            tradeEvent.instrumentId(),
            tradeEvent.eventId(),
            tradeDate,
            tradeEvent.price(),
            tradeEvent.quantity(),
            tradeEvent.eventTime(),
            tradeEvent.quantity(),
            tradeEvent.price(),
            tradeEvent.price(),
            1L,
            Instant.now(clock),
            tradeEvent.currency()
        );
    }

    private LocalDate tradingDateOf(Instant eventTime) {
        return LocalDate.ofInstant(eventTime, ZoneOffset.UTC);
    }

    private void ensureSameInstrument(InstrumentSnapshot snapshot, TradeEvent tradeEvent) {
        if (!snapshot.instrumentId().equals(tradeEvent.instrumentId())) {
            throw new IllegalArgumentException(
                "Snapshot instrument '%s' does not match trade instrumentId '%s'"
                    .formatted(snapshot.instrumentId(), tradeEvent.instrumentId())
            );
        }

        if (!snapshot.currency().equalsIgnoreCase(tradeEvent.currency())) {
            throw new IllegalArgumentException(
                "Snapshot currency '%s' does not match trade currency '%s'"
                    .formatted(snapshot.currency(), tradeEvent.currency())
            );
        }
    }
}
