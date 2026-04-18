package com.github.nfmdev.candlestack.processing_service.persistence.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.github.nfmdev.candlestack.processing_service.persistence.entity.InstrumentSnapshotEntity;

public interface InstrumentSnapshotJpaRepository extends JpaRepository<InstrumentSnapshotEntity, String> {
}
