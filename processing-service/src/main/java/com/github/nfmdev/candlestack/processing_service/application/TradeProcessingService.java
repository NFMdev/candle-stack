package com.github.nfmdev.candlestack.processing_service.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.nfmdev.candlestack.processing_service.domain.event.MarketTradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.event.TradeEvent;
import com.github.nfmdev.candlestack.processing_service.domain.mapper.TradeEventMapper;
import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.domain.model.SnapshotUpdateResult;
import com.github.nfmdev.candlestack.processing_service.domain.service.InstrumentSnapshotCalculator;
import com.github.nfmdev.candlestack.processing_service.domain.validation.TradeEventValidator;
import com.github.nfmdev.candlestack.processing_service.persistence.adapter.InstrumentSnapshotPersistenceAdapter;
import com.github.nfmdev.candlestack.processing_service.state.InstrumentStateStore;

@Service
public class TradeProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TradeProcessingService.class);

    private final TradeEventMapper tradeEventMapper;
    private final TradeEventValidator tradeEventValidator;
    private final InstrumentSnapshotCalculator snapshotCalculator;
    private final InstrumentSnapshotPersistenceAdapter persistenceAdapter;
    private final InstrumentStateStore stateStore;

    public TradeProcessingService(
            TradeEventMapper tradeEventMapper,
            TradeEventValidator tradeEventValidator,
            InstrumentSnapshotCalculator snapshotCalculator,
            InstrumentSnapshotPersistenceAdapter persistenceAdapter,
            InstrumentStateStore stateStore
    ) {
        this.tradeEventMapper = tradeEventMapper;
        this.tradeEventValidator = tradeEventValidator;
        this.snapshotCalculator = snapshotCalculator;
        this.persistenceAdapter = persistenceAdapter;
        this.stateStore = stateStore;
    }

    public void process(MarketTradeEvent marketTradeEvent) {
        TradeEvent tradeEvent = tradeEventMapper.toCanonical(marketTradeEvent);
        tradeEventValidator.validate(tradeEvent);

        InstrumentSnapshot currentSnapshot = stateStore.get(tradeEvent.instrumentId()).orElse(null);

        SnapshotUpdateResult updateResult = snapshotCalculator.apply(currentSnapshot, tradeEvent);

        if (!updateResult.applied()) {
            log.debug(
                    "Trade ignored. instrumentId={}, eventId={}, decision={}, reason={}",
                    tradeEvent.instrumentId(),
                    tradeEvent.eventId(),
                    updateResult.decision(),
                    updateResult.reason()
            );
            return;
        }

        InstrumentSnapshot newSnapshot = updateResult.snapshot();

        persistenceAdapter.upsert(newSnapshot);
        stateStore.put(newSnapshot);

        log.debug(
                "Trade applied. instrumentId={}, eventId={}, decision={}, tradingDate={}, lastPrice={}",
                newSnapshot.instrumentId(),
                newSnapshot.lastEventId(),
                updateResult.decision(),
                newSnapshot.tradingDate(),
                newSnapshot.lastPrice()
        );
    }
}