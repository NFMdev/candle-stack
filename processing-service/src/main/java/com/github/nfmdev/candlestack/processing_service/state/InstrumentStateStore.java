package com.github.nfmdev.candlestack.processing_service.state;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;

@Component
public class InstrumentStateStore {
    private final ConcurrentMap<String, InstrumentSnapshot> snapshots = new ConcurrentHashMap<>();

    public Optional<InstrumentSnapshot> get(String instrumentId) {
        return Optional.ofNullable(snapshots.get(instrumentId));
    }

    public void put(InstrumentSnapshot snapshot) {
        snapshots.put(snapshot.instrumentId(), snapshot);
    }

    public void remove(String instrumentId) {
        snapshots.remove(instrumentId);
    }

    public int size() {
        return snapshots.size();
    }
}