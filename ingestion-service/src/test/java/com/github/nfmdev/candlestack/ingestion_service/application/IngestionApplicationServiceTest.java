package com.github.nfmdev.candlestack.ingestion_service.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;

import com.github.nfmdev.candlestack.ingestion_service.api.dto.EventAcceptedResponse;
import com.github.nfmdev.candlestack.ingestion_service.api.dto.MarketTradeEventRequest;
import com.github.nfmdev.candlestack.ingestion_service.domain.model.TradeSide;
import com.github.nfmdev.candlestack.ingestion_service.domain.ports.MarketEventPublisherPort;

class IngestionApplicationServiceTest {
    @Test
    void ingestShouldPublishEventAndReturnAcceptedResponse() {
        MarketEventPublisherPort publisher =  Mockito.mock(MarketEventPublisherPort.class);
        IngestionApplicationService service = new IngestionApplicationService(publisher);

        UUID eventId = UUID.randomUUID();

        MarketTradeEventRequest request = new MarketTradeEventRequest(
            eventId, 
            UUID.randomUUID(),
            "generator-service",
            "TRADE",
            "BTC-USD",
            Instant.now(),
            1l,
            new BigDecimal("68000.50"),
            new BigDecimal("0.250000"),
            TradeSide.BUY,
            "USD",
            null
        );

        EventAcceptedResponse response = service.ingest(request);

        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.eventId()).isEqualTo(eventId);
        verify(publisher).publish(Mockito.any());
    }
}
