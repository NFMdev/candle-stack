package com.github.nfmdev.candlestack.processing_service.application;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.persistence.adapter.InstrumentSnapshotPersistenceAdapter;
import com.github.nfmdev.candlestack.processing_service.state.InstrumentStateStore;

@Service
public class SnapshotStateRehydrationService {
    private static final Logger log = LoggerFactory.getLogger(SnapshotStateRehydrationService.class);

    private final InstrumentSnapshotPersistenceAdapter persistenceAdapter;
    private final InstrumentStateStore stateStore;

    public SnapshotStateRehydrationService(
        InstrumentSnapshotPersistenceAdapter persistenceAdapter,
        InstrumentStateStore stateStore
    ) {
        this.persistenceAdapter = persistenceAdapter;
        this.stateStore = stateStore;
    }

    public void rehydrate() {
        List<InstrumentSnapshot> snapshots = persistenceAdapter.findAllSnapshots();

        stateStore.clear();
        stateStore.putAll(snapshots);

        log.info("Instrument state rehydrated. loadedSnapshots={}", snapshots.size());
    }
}
