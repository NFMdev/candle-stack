package com.github.nfmdev.candlestack.processing_service.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeSide;
import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.domain.model.SnapshotUpdateResult;
import com.github.nfmdev.candlestack.processing_service.domain.model.UpdateDecision;

class InstrumentSnapshotCalculatorTest {
    private final Instant fixedNow = Instant.parse("2026-04-20T10:30:00Z");
    private final Clock clock = Clock.fixed(fixedNow, ZoneOffset.UTC);
    private final InstrumentSnapshotCalculator calculator = new InstrumentSnapshotCalculator(clock);

    @Test
    void shouldCreateSnapshotOnFirstTrade() {
        LocalDate expectedTradeDate = LocalDate.of(2026, 4, 20);

        TradeEvent tradeEvent = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "BTC-USD",
            fixedNow,
            1L,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD"
        );
        
        SnapshotUpdateResult result = calculator.apply(null, tradeEvent);
        
        assertThat(result.decision()).isEqualTo(UpdateDecision.CREATED);
        var snapshot = result.snapshot();
        assertThat(snapshot.instrumentId()).isEqualTo("BTC-USD");
        assertThat(snapshot.lastEventId()).isEqualTo(tradeEvent.eventId()); 
        assertThat(snapshot.tradingDate()).isEqualTo(expectedTradeDate);
        assertThat(snapshot.lastPrice()).isEqualTo(tradeEvent.price());
        assertThat(snapshot.lastQuantity()).isEqualTo(tradeEvent.quantity());
        assertThat(snapshot.lastEventTime()).isEqualTo(tradeEvent.eventTime());
        assertThat(snapshot.dayVolume()).isEqualTo(tradeEvent.quantity());
        assertThat(snapshot.dayHigh()).isEqualTo(tradeEvent.price());
        assertThat(snapshot.dayLow()).isEqualTo(tradeEvent.price());
        assertThat(snapshot.tradeCount()).isEqualTo(1L);
        assertThat(snapshot.updatedAt()).isEqualTo(fixedNow);
        assertThat(snapshot.currency()).isEqualTo(tradeEvent.currency());
    }

    @Test
    void shouldUpdateSnapshotOnTrade() {
        // Given: an existing snapshot from a previous trade
        UUID initialEventId = UUID.randomUUID();
        Instant initialEventTime = Instant.parse("2026-04-20T10:29:00Z");
        InstrumentSnapshot initialSnapshot = new InstrumentSnapshot(
            "BTC-USD",
            initialEventId,
            LocalDate.of(2026, 4, 20),
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            initialEventTime,
            new BigDecimal("0.250000"), // dayVolume
            new BigDecimal("68000.50"), // dayHigh
            new BigDecimal("68000.50"), // dayLow
            1L,
            initialEventTime, // updatedAt
            "USD"
        );

        // And: a new trade event for the same day
        TradeEvent newTradeEvent = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "BTC-USD",
            fixedNow, // This is the event time for the new trade
            2L,
            new BigDecimal("68000.00"),
            new BigDecimal("0.240000"),
            TradeSide.SELL,
            "USD"
        );

        // When: the calculator applies the new trade
        SnapshotUpdateResult result = calculator.apply(initialSnapshot, newTradeEvent);

        // Then: the snapshot should be updated correctly
        assertThat(result.decision()).isEqualTo(UpdateDecision.UPDATED);
        var updatedSnapshot = result.snapshot();
        assertThat(updatedSnapshot.lastEventId()).isEqualTo(newTradeEvent.eventId());
        assertThat(updatedSnapshot.lastEventTime()).isEqualTo(newTradeEvent.eventTime());
        assertThat(updatedSnapshot.lastPrice()).isEqualTo(newTradeEvent.price());
        assertThat(updatedSnapshot.lastQuantity()).isEqualTo(newTradeEvent.quantity());
        assertThat(updatedSnapshot.dayVolume()).isEqualTo(new BigDecimal("0.490000"));
        assertThat(updatedSnapshot.dayHigh()).isEqualTo(new BigDecimal("68000.50"));
        assertThat(updatedSnapshot.dayLow()).isEqualTo(new BigDecimal("68000.00"));
        assertThat(updatedSnapshot.tradeCount()).isEqualTo(2L);
        assertThat(updatedSnapshot.updatedAt()).isEqualTo(fixedNow);
    }

    @Test
    void shouldReturnIgnoredDuplicate() {
        // Given: an existing snapshot from a previous trade
        UUID initialEventId = UUID.randomUUID();
        Instant initialEventTime = Instant.parse("2026-04-20T10:29:00Z");
        InstrumentSnapshot initialSnapshot = new InstrumentSnapshot(
            "BTC-USD",
            initialEventId,
            LocalDate.of(2026, 4, 20),
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            initialEventTime,
            new BigDecimal("0.250000"), // dayVolume
            new BigDecimal("68000.50"), // dayHigh
            new BigDecimal("68000.50"), // dayLow
            1L,
            initialEventTime, // updatedAt
            "USD"
        );

        // And: a new trade event with the same eventId
        TradeEvent newTradeEvent = new TradeEvent(
            initialEventId,
            UUID.randomUUID(),
            "generator-service",
            "BTC-USD",
            fixedNow,
            2L,
            new BigDecimal("68000.00"),
            new BigDecimal("0.240000"),
            TradeSide.BUY,
            "USD"
        );

        // When: the calculator applies the new trade
        SnapshotUpdateResult result = calculator.apply(initialSnapshot, newTradeEvent);

        // Then: the snapshot should be ignored
        assertThat(result.decision()).isEqualTo(UpdateDecision.IGNORED_DUPLICATE);
        assertThat(result.snapshot()).isEqualTo(initialSnapshot);
        assertThat(result.reason()).isEqualTo("Duplicate eventId: " + initialEventId);
    }

    @Test
    void shouldReturnIgnoredOlderTradingDate() {
        // Given: an existing snapshot from a previous trade
        InstrumentSnapshot initialSnapshot = new InstrumentSnapshot(
            "BTC-USD",
            UUID.randomUUID(),
            LocalDate.of(2026, 4, 20),
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            fixedNow,
            new BigDecimal("0.250000"), // dayVolume
            new BigDecimal("68000.50"), // dayHigh
            new BigDecimal("68000.50"), // dayLow
            1L,
            fixedNow, // updatedAt
            "USD"
        );

        // And: a new trade event with 
        TradeEvent newTradeEvent = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "BTC-USD",
            Instant.parse("2026-04-19T10:30:00Z"), // -1 day
            2L,
            new BigDecimal("68000.00"),
            new BigDecimal("0.240000"),
            TradeSide.BUY,
            "USD"
        );

        // When: the calculator applies the new trade
        SnapshotUpdateResult result = calculator.apply(initialSnapshot, newTradeEvent);

        // Then: the snapshot should be ignored
        assertThat(result.decision()).isEqualTo(UpdateDecision.IGNORED_OLDER_TRADING_DATE);
        assertThat(result.snapshot()).isEqualTo(initialSnapshot);
        assertThat(result.reason()).isEqualTo("Trade tradingDate is older than current snapshot tradingDate");
    }

    @Test
    void shouldReturnIgnoredLateEvent() {
        // Given: an existing snapshot from a previous trade
        InstrumentSnapshot initialSnapshot = new InstrumentSnapshot(
            "BTC-USD",
            UUID.randomUUID(),
            LocalDate.of(2026, 4, 20),
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            fixedNow,
            new BigDecimal("0.250000"), // dayVolume
            new BigDecimal("68000.50"), // dayHigh
            new BigDecimal("68000.50"), // dayLow
            1L,
            fixedNow, // updatedAt
            "USD"
        );

        // And: a new trade event with 
        TradeEvent newTradeEvent = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "BTC-USD",
            Instant.parse("2026-04-20T10:29:00Z"), // -1 minute
            2L,
            new BigDecimal("68000.00"),
            new BigDecimal("0.240000"),
            TradeSide.BUY,
            "USD"
        );

        // When: the calculator applies the new trade
        SnapshotUpdateResult result = calculator.apply(initialSnapshot, newTradeEvent);

        // Then: the snapshot should be ignored
        assertThat(result.decision()).isEqualTo(UpdateDecision.IGNORED_LATE_EVENT);
        assertThat(result.snapshot()).isEqualTo(initialSnapshot);
        assertThat(result.reason()).isEqualTo("Trade eventTime is older than snapshot lastEventTime");
    }

    @Test
    void shouldReturnRolledOver() {
        // Given: an existing snapshot from a previous trade
        InstrumentSnapshot initialSnapshot = new InstrumentSnapshot(
            "BTC-USD",
            UUID.randomUUID(),
            LocalDate.of(2026, 4, 20),
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            fixedNow,
            new BigDecimal("0.250000"), // dayVolume
            new BigDecimal("68000.50"), // dayHigh
            new BigDecimal("68000.50"), // dayLow
            1L,
            fixedNow, // updatedAt
            "USD"
        );

        // And: a new trade event with 
        TradeEvent newTradeEvent = new TradeEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "generator-service",
            "BTC-USD",
            Instant.parse("2026-04-21T10:29:00Z"), // +1 day
            2L,
            new BigDecimal("68000.00"),
            new BigDecimal("0.240000"),
            TradeSide.BUY,
            "USD"
        );

        // When: the calculator applies the new trade
        SnapshotUpdateResult result = calculator.apply(initialSnapshot, newTradeEvent);

        // Then: the snapshot should be updated
        assertThat(result.decision()).isEqualTo(UpdateDecision.ROLLED_OVER);
        var updatedSnapshot = result.snapshot();
        assertThat(updatedSnapshot.lastEventId()).isEqualTo(newTradeEvent.eventId());
        assertThat(updatedSnapshot.tradingDate()).isEqualTo(LocalDate.of(2026, 4, 21));
        assertThat(updatedSnapshot.lastEventTime()).isEqualTo(newTradeEvent.eventTime());
        assertThat(updatedSnapshot.lastPrice()).isEqualTo(newTradeEvent.price());
        assertThat(updatedSnapshot.lastQuantity()).isEqualTo(newTradeEvent.quantity());
        assertThat(updatedSnapshot.dayVolume()).isEqualTo(new BigDecimal("0.240000"));
        assertThat(updatedSnapshot.dayHigh()).isEqualTo(new BigDecimal("68000.00"));
        assertThat(updatedSnapshot.dayLow()).isEqualTo(new BigDecimal("68000.00"));
        assertThat(updatedSnapshot.tradeCount()).isEqualTo(1L);
    }
}
