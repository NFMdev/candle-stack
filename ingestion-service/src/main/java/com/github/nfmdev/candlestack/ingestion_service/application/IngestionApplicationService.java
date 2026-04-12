package com.github.nfmdev.candlestack.ingestion_service.application;

import org.springframework.stereotype.Service;

import com.github.nfmdev.candlestack.ingestion_service.api.dto.EventAcceptedResponse;
import com.github.nfmdev.candlestack.ingestion_service.api.dto.MarketTradeEventRequest;
import com.github.nfmdev.candlestack.ingestion_service.domain.model.MarketTradeEvent;
import com.github.nfmdev.candlestack.ingestion_service.domain.ports.MarketEventPublisherPort;

@Service
public class IngestionApplicationService {
    private final MarketEventPublisherPort eventPublisher;

    public IngestionApplicationService(MarketEventPublisherPort eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public EventAcceptedResponse ingest(MarketTradeEventRequest request) {
        MarketTradeEvent event = toDomain(request);
        eventPublisher.publish(event);

        return new EventAcceptedResponse("ACCEPTED", event.eventId());
    }

    private MarketTradeEvent toDomain(MarketTradeEventRequest request) {
        return new MarketTradeEvent(
            request.eventId(),
            request.scenarioId(),
            request.source(),
            request.eventType(),
            request.symbol(),
            request.eventTime(),
            request.sequence(),
            request.price(),
            request.quantity(),
            request.side(),
            request.currency()
        );
    }
}
