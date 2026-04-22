package com.github.nfmdev.candlestack.processing_service.application;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeSide;
import com.github.nfmdev.candlestack.processing_service.domain.mapper.TradeEventMapper;
import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.domain.model.SnapshotUpdateResult;
import com.github.nfmdev.candlestack.processing_service.domain.model.UpdateDecision;
import com.github.nfmdev.candlestack.processing_service.domain.service.InstrumentSnapshotCalculator;
import com.github.nfmdev.candlestack.processing_service.domain.validation.TradeEventValidator;
import com.github.nfmdev.candlestack.processing_service.persistence.adapter.InstrumentSnapshotPersistenceAdapter;
import com.github.nfmdev.candlestack.processing_service.state.InstrumentStateStore;
import com.github.nfmdev.candlestack.processing_service.support.exception.InvalidTradeEventException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeProcessingServiceTest {
    @Mock
    private TradeEventMapper tradeEventMapper;
    @Mock
    private TradeEventValidator tradeEventValidator;
    @Mock
    private InstrumentSnapshotCalculator snapshotCalculator;
    @Mock
    private InstrumentSnapshotPersistenceAdapter persistenceAdapter;
    @Mock
    private InstrumentStateStore stateStore;

    @InjectMocks
    private TradeProcessingService tradeProcessingService;

    private MarketTradeEvent marketTradeEvent;
    private TradeEvent tradeEvent;
    private InstrumentSnapshot currentSnapshot;
    private InstrumentSnapshot newSnapshot;

    @BeforeEach
    void setUp() {
        UUID eventId = UUID.randomUUID();
        UUID scenarioId = UUID.randomUUID();
        Instant eventTime = Instant.now();

        marketTradeEvent = new MarketTradeEvent(
                eventId, scenarioId, "generator-service", "TRADE", "USD-BTC", eventTime,
                1L, new BigDecimal("69000.00"), new BigDecimal("0.240000"),
                TradeSide.BUY, "USD"
        );

        tradeEvent = new TradeEvent(
                eventId, scenarioId, "generator-service", "USD-BTC", eventTime,
                1L, new BigDecimal("69000.00"), new BigDecimal("0.240000"),
                TradeSide.BUY, "USD"
        );

        currentSnapshot = new InstrumentSnapshot(
                "USD-BTC", eventId, LocalDate.of(2026, 4, 20),
                new BigDecimal("67000.00"), new BigDecimal("0.250000"), eventTime,
                new BigDecimal("0.250000"), new BigDecimal("67000.00"),
                new BigDecimal("67000.00"), 1L, eventTime, "USD"
        );

        newSnapshot = new InstrumentSnapshot(
                "USD-BTC", UUID.randomUUID(), LocalDate.of(2026, 4, 20),
                new BigDecimal("69000.00"), new BigDecimal("0.240000"), eventTime,
                new BigDecimal("0.490000"), new BigDecimal("69000.00"),
                new BigDecimal("67000.00"), 2L, eventTime, "USD"
        );
    }

    @Test
    void process_shouldApplyTrade_andPersistBeforeUpdatingState() {
        when(tradeEventMapper.toCanonical(marketTradeEvent)).thenReturn(tradeEvent);
        when(stateStore.get(tradeEvent.instrumentId())).thenReturn(Optional.of(currentSnapshot));
        when(snapshotCalculator.apply(currentSnapshot, tradeEvent))
                .thenReturn(new SnapshotUpdateResult(UpdateDecision.UPDATED, newSnapshot, null));

        tradeProcessingService.process(marketTradeEvent);

        InOrder inOrder = inOrder(persistenceAdapter, stateStore);

        inOrder.verify(persistenceAdapter).upsert(newSnapshot);
        inOrder.verify(stateStore).put(newSnapshot);
    }

    @Test
    void process_shouldIgnoreTrade_whenNotApplied() {
        when(tradeEventMapper.toCanonical(marketTradeEvent)).thenReturn(tradeEvent);
        when(stateStore.get(tradeEvent.instrumentId())).thenReturn(Optional.of(currentSnapshot));
        when(snapshotCalculator.apply(currentSnapshot, tradeEvent))
                .thenReturn(new SnapshotUpdateResult(UpdateDecision.IGNORED_DUPLICATE, currentSnapshot, null));

        tradeProcessingService.process(marketTradeEvent);

        verify(persistenceAdapter, never()).upsert(any());
        verify(stateStore, never()).put(any());
    }

    @Test
    void process_shouldThrowException_whenEventIsInvalid() {
        when(tradeEventMapper.toCanonical(marketTradeEvent)).thenReturn(tradeEvent);
        doThrow(new InvalidTradeEventException("Invalid event"))
                .when(tradeEventValidator).validate(tradeEvent);

        assertThrows(
                InvalidTradeEventException.class,
                () -> tradeProcessingService.process(marketTradeEvent)
        );

        verifyNoInteractions(snapshotCalculator);
        verifyNoInteractions(persistenceAdapter);
        verifyNoInteractions(stateStore);
    }

    @Test
    void process_shouldHandleNewInstrument_whenNoExistingSnapshot() {
        when(tradeEventMapper.toCanonical(marketTradeEvent)).thenReturn(tradeEvent);
        when(stateStore.get(tradeEvent.instrumentId())).thenReturn(Optional.empty());
        when(snapshotCalculator.apply(null, tradeEvent))
                .thenReturn(new SnapshotUpdateResult(UpdateDecision.CREATED, newSnapshot, null));

        tradeProcessingService.process(marketTradeEvent);

        verify(persistenceAdapter).upsert(newSnapshot);
        verify(stateStore).put(newSnapshot);
    }

    @Test
    void process_shouldPropagateException_whenPersistenceFails() {
        when(tradeEventMapper.toCanonical(marketTradeEvent)).thenReturn(tradeEvent);
        when(stateStore.get(tradeEvent.instrumentId())).thenReturn(Optional.of(currentSnapshot));
        when(snapshotCalculator.apply(currentSnapshot, tradeEvent))
                .thenReturn(new SnapshotUpdateResult(UpdateDecision.UPDATED, newSnapshot, null));

        doThrow(new RuntimeException("DB error"))
                .when(persistenceAdapter).upsert(newSnapshot);

        assertThrows(RuntimeException.class,
                () -> tradeProcessingService.process(marketTradeEvent));

        verify(stateStore, never()).put(any());
    }
}
