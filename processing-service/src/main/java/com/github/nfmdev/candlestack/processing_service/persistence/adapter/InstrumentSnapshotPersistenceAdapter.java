package com.github.nfmdev.candlestack.processing_service.persistence.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.persistence.entity.InstrumentSnapshotEntity;
import com.github.nfmdev.candlestack.processing_service.persistence.repository.InstrumentSnapshotJpaRepository;

@Component
public class InstrumentSnapshotPersistenceAdapter {

    private final InstrumentSnapshotJpaRepository repository;

    public InstrumentSnapshotPersistenceAdapter(InstrumentSnapshotJpaRepository repository) {
        this.repository = repository;
    }

    public void upsert(InstrumentSnapshot snapshot) {
        repository.save(toEntity(snapshot));
    }

    public List<InstrumentSnapshot> findAllSnapshots() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private InstrumentSnapshotEntity toEntity(InstrumentSnapshot snapshot) {
        InstrumentSnapshotEntity entity = new InstrumentSnapshotEntity();
        entity.setInstrumentId(snapshot.instrumentId());
        entity.setLastEventId(snapshot.lastEventId());
        entity.setTradingDate(snapshot.tradingDate());
        entity.setLastPrice(snapshot.lastPrice());
        entity.setLastQuantity(snapshot.lastQuantity());
        entity.setLastEventTime(snapshot.lastEventTime());
        entity.setDayVolume(snapshot.dayVolume());
        entity.setDayHigh(snapshot.dayHigh());
        entity.setDayLow(snapshot.dayLow());
        entity.setTradeCount(snapshot.tradeCount());
        entity.setUpdatedAt(snapshot.updatedAt());
        entity.setCurrency(snapshot.currency());
        return entity;
    }

    private InstrumentSnapshot toDomain(InstrumentSnapshotEntity entity) {
        return new InstrumentSnapshot(
            entity.getInstrumentId(),
            entity.getLastEventId(),
            entity.getTradingDate(),
            entity.getLastPrice(),
            entity.getLastQuantity(),
            entity.getLastEventTime(),
            entity.getDayVolume(),
            entity.getDayHigh(),
            entity.getDayLow(),
            entity.getTradeCount(),
            entity.getUpdatedAt(),
            entity.getCurrency()
        );
    }
}