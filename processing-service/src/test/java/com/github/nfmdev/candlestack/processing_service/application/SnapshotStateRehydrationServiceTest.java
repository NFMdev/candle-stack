package com.github.nfmdev.candlestack.processing_service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.github.nfmdev.candlestack.processing_service.domain.model.InstrumentSnapshot;
import com.github.nfmdev.candlestack.processing_service.persistence.entity.InstrumentSnapshotEntity;
import com.github.nfmdev.candlestack.processing_service.persistence.repository.InstrumentSnapshotJpaRepository;
import com.github.nfmdev.candlestack.processing_service.state.InstrumentStateStore;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
class SnapshotStateRehydrationServiceTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("candlestack-test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private InstrumentSnapshotJpaRepository repository;

    @Autowired
    private SnapshotStateRehydrationService rehydrationService;

    @Autowired
    private InstrumentStateStore stateStore;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        stateStore.clear();
    }

    @Test
    void shouldRehydrateStateStoreFromDatabase() {
        // given
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();

        InstrumentSnapshotEntity entity = new InstrumentSnapshotEntity();
        entity.setInstrumentId("BTC-USD");
        entity.setLastEventId(eventId);
        entity.setTradingDate(LocalDate.of(2026, 4, 20));
        entity.setLastPrice(new BigDecimal("65000.00"));
        entity.setLastQuantity(new BigDecimal("0.50"));
        entity.setLastEventTime(now);
        entity.setDayVolume(new BigDecimal("10.00"));
        entity.setDayHigh(new BigDecimal("66000.00"));
        entity.setDayLow(new BigDecimal("64000.00"));
        entity.setTradeCount(42L);
        entity.setUpdatedAt(now);
        entity.setCurrency("USD");

        repository.save(entity);

        // when
        rehydrationService.rehydrate();

        // then
        assertThat(stateStore.size()).isEqualTo(1);

        InstrumentSnapshot snapshot = stateStore.get("BTC-USD").orElseThrow();

        assertThat(snapshot.instrumentId()).isEqualTo("BTC-USD");
        assertThat(snapshot.lastEventId()).isEqualTo(eventId);
        assertThat(snapshot.lastPrice()).isEqualByComparingTo("65000.00");
        assertThat(snapshot.dayVolume()).isEqualByComparingTo("10.00");
        assertThat(snapshot.tradeCount()).isEqualTo(42L);
        assertThat(snapshot.currency()).isEqualTo("USD");
    }

    @Test
    void shouldClearExistingStateBeforeRehydration() {
        // given
        stateStore.put(new InstrumentSnapshot(
                "ETH-USD",
                UUID.randomUUID(),
                LocalDate.now(),
                new BigDecimal("2000"),
                new BigDecimal("1"),
                Instant.now(),
                new BigDecimal("1"),
                new BigDecimal("2000"),
                new BigDecimal("2000"),
                1L,
                Instant.now(),
                "USD"
        ));

        InstrumentSnapshotEntity entity = new InstrumentSnapshotEntity();
        entity.setInstrumentId("BTC-USD");
        entity.setLastEventId(UUID.randomUUID());
        entity.setTradingDate(LocalDate.now());
        entity.setLastPrice(new BigDecimal("65000"));
        entity.setLastQuantity(new BigDecimal("1"));
        entity.setLastEventTime(Instant.now());
        entity.setDayVolume(new BigDecimal("5"));
        entity.setDayHigh(new BigDecimal("66000"));
        entity.setDayLow(new BigDecimal("64000"));
        entity.setTradeCount(10L);
        entity.setUpdatedAt(Instant.now());
        entity.setCurrency("USD");

        repository.save(entity);

        // when
        rehydrationService.rehydrate();

        // then
        assertThat(stateStore.size()).isEqualTo(1);
        assertThat(stateStore.get("ETH-USD")).isEmpty();
        assertThat(stateStore.get("BTC-USD")).isPresent();
    }

    @Test
    void shouldHandleEmptyDatabase() {
        rehydrationService.rehydrate();
        assertThat(stateStore.size()).isZero();
    }
}